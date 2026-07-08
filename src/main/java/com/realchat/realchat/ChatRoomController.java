package com.realchat.realchat;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/*
 * ChatRoomController
 *
 * 채팅방 관련 API를 처리하는 컨트롤러.
 * - GET  /api/chat/users       → 전체 유저 목록 (접속 유저 탭용)
 * - GET  /api/chat/rooms       → 내가 속한 채팅방 목록
 * - POST /api/chat/dm          → 1:1 채팅방 생성
 * - POST /api/chat/rooms       → 단체 채팅방 생성
 * - POST /api/chat/rooms/{id}/invite → 멤버 초대
 */
@RestController
@RequestMapping("/api/chat")
public class ChatRoomController {

	private final UserRepository userRepository;
	private final ChatRoomRepository chatRoomRepository;
	private final ChatRoomMemberRepository chatRoomMemberRepository;
	private final MessageRepository messageRepository;
	private final JwtUtil jwtUtil;

	public ChatRoomController(UserRepository userRepository,
	                          ChatRoomRepository chatRoomRepository,
	                          ChatRoomMemberRepository chatRoomMemberRepository,
	                          MessageRepository messageRepository,
	                          JwtUtil jwtUtil) {
	    this.userRepository = userRepository;
	    this.chatRoomRepository = chatRoomRepository;
	    this.chatRoomMemberRepository = chatRoomMemberRepository;
	    this.messageRepository = messageRepository;
	    this.jwtUtil = jwtUtil;
    }

    // 토큰에서 유저 꺼내기 (공통으로 쓰는 메서드)
    private User getUserFromToken(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String username = jwtUtil.getUsername(token);
        return userRepository.findByUsername(username).orElse(null);
    }

    // 전체 유저 목록 (나 제외)
    @GetMapping("/users")
    public ResponseEntity<?> getUsers(@RequestHeader("Authorization") String authHeader) {
        User me = getUserFromToken(authHeader);
        if (me == null) return ResponseEntity.badRequest().body(Map.of("error", "인증 실패"));

        List<Map<String, Object>> users = userRepository.findAll().stream()
            .filter(u -> !u.getId().equals(me.getId()))
            .map(u -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", u.getId());
                map.put("username", u.getUsername());
                map.put("nickname", u.getNickname());
                map.put("online", u.isOnline());
                return map;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(users);
    }

    // 내가 속한 채팅방 목록
    @GetMapping("/rooms")
    public ResponseEntity<?> getMyRooms(@RequestHeader("Authorization") String authHeader) {
        User me = getUserFromToken(authHeader);
        if (me == null) return ResponseEntity.badRequest().body(Map.of("error", "인증 실패"));

        List<Map<String, Object>> rooms = chatRoomMemberRepository.findByUser(me).stream()
            .map(member -> {
                ChatRoom room = member.getChatRoom();
                Map<String, Object> map = new HashMap<>();
                map.put("id", room.getId());
                map.put("type", room.getType());
                map.put("title", getRoomTitle(room, me));

                // 안 읽은 메시지 수
                Long lastRead = member.getLastReadMessageId();
                if (lastRead == null) lastRead = 0L;
                long unread = messageRepository.countByRoomIdAndIdGreaterThan(room.getId(), lastRead);
                map.put("unread", unread);

                return map;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(rooms);
    }

    // 1:1 채팅방 생성 (이미 있으면 기존 방 반환)
    @PostMapping("/dm")
    public ResponseEntity<?> createDm(@RequestHeader("Authorization") String authHeader,
                                      @RequestBody Map<String, String> request) {
        User me = getUserFromToken(authHeader);
        if (me == null) return ResponseEntity.badRequest().body(Map.of("error", "인증 실패"));

        String targetNickname = request.get("nickname");
        Optional<User> targetOptional = userRepository.findByNickname(targetNickname);

        if (targetOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "존재하지 않는 유저입니다"));
        }

        User target = targetOptional.get();

        if (target.getId().equals(me.getId())) {
            return ResponseEntity.badRequest().body(Map.of("error", "자기 자신과는 채팅할 수 없습니다"));
        }

        // 이미 DM이 있는지 확인
        ChatRoom existingDm = findExistingDm(me, target);
        if (existingDm != null) {
            return ResponseEntity.ok(Map.of(
                "id", existingDm.getId(),
                "type", "DM",
                "title", target.getNickname()
            ));
        }

        // 새 DM 생성
        ChatRoom dm = new ChatRoom("DM", null);
        chatRoomRepository.save(dm);
        chatRoomMemberRepository.save(new ChatRoomMember(dm, me));
        chatRoomMemberRepository.save(new ChatRoomMember(dm, target));

        return ResponseEntity.ok(Map.of(
            "id", dm.getId(),
            "type", "DM",
            "title", target.getNickname()
        ));
    }

    // 단체 채팅방 생성
    @PostMapping("/rooms")
    public ResponseEntity<?> createGroupRoom(@RequestHeader("Authorization") String authHeader,
                                             @RequestBody Map<String, Object> request) {
        User me = getUserFromToken(authHeader);
        if (me == null) return ResponseEntity.badRequest().body(Map.of("error", "인증 실패"));

        String title = (String) request.get("title");
        List<String> nicknames = (List<String>) request.get("nicknames");

        if (title == null || title.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "방 제목을 입력하세요"));
        }

        // 방 생성
        ChatRoom room = new ChatRoom("GROUP", title);
        chatRoomRepository.save(room);

        // 나를 멤버로 추가
        chatRoomMemberRepository.save(new ChatRoomMember(room, me));

        // 초대할 사람들 추가
        if (nicknames != null) {
            for (String nickname : nicknames) {
                userRepository.findByNickname(nickname).ifPresent(user ->
                    chatRoomMemberRepository.save(new ChatRoomMember(room, user))
                );
            }
        }

        return ResponseEntity.ok(Map.of(
            "id", room.getId(),
            "type", "GROUP",
            "title", title
        ));
    }

    // 멤버 초대
    @PostMapping("/rooms/{roomId}/invite")
    public ResponseEntity<?> invite(@RequestHeader("Authorization") String authHeader,
            @PathVariable("roomId") Long roomId,
                                    @RequestBody Map<String, String> request) {
        User me = getUserFromToken(authHeader);
        if (me == null) return ResponseEntity.badRequest().body(Map.of("error", "인증 실패"));

        Optional<ChatRoom> roomOptional = chatRoomRepository.findById(roomId);
        if (roomOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "존재하지 않는 채팅방입니다"));
        }

        ChatRoom room = roomOptional.get();
        String targetNickname = request.get("nickname");
        Optional<User> targetOptional = userRepository.findByNickname(targetNickname);

        if (targetOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "존재하지 않는 유저입니다"));
        }

        User target = targetOptional.get();

        // 이미 방에 있는지 확인
        if (chatRoomMemberRepository.existsByChatRoomAndUser(room, target)) {
            return ResponseEntity.badRequest().body(Map.of("error", "이미 채팅방에 있는 유저입니다"));
        }

        chatRoomMemberRepository.save(new ChatRoomMember(room, target));

        return ResponseEntity.ok(Map.of("message", targetNickname + "님을 초대했습니다"));
    }

    // DM 방 제목: 상대방 닉네임으로 표시
    private String getRoomTitle(ChatRoom room, User me) {
        if ("GROUP".equals(room.getType())) {
            return room.getTitle();
        }
        // DM이면 상대방 닉네임 표시
        return chatRoomMemberRepository.findByChatRoom(room).stream()
            .map(ChatRoomMember::getUser)
            .filter(u -> !u.getId().equals(me.getId()))
            .map(User::getNickname)
            .findFirst()
            .orElse("알 수 없음");
    }

    // 기존 DM 찾기
    private ChatRoom findExistingDm(User user1, User user2) {
        List<ChatRoomMember> myRooms = chatRoomMemberRepository.findByUser(user1);
        for (ChatRoomMember member : myRooms) {
            ChatRoom room = member.getChatRoom();
            if ("DM".equals(room.getType())) {
                boolean hasTarget = chatRoomMemberRepository.existsByChatRoomAndUser(room, user2);
                if (hasTarget) return room;
            }
        }
        return null;
    }
    
 // 채팅방 멤버 목록 조회
    @GetMapping("/rooms/{roomId}/members")
    public ResponseEntity<?> getMembers(@RequestHeader("Authorization") String authHeader,
                                        @PathVariable("roomId") Long roomId) {
        User me = getUserFromToken(authHeader);
        if (me == null) return ResponseEntity.badRequest().body(Map.of("error", "인증 실패"));

        Optional<ChatRoom> roomOptional = chatRoomRepository.findById(roomId);
        if (roomOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "존재하지 않는 채팅방입니다"));
        }

        List<Map<String, Object>> members = chatRoomMemberRepository.findByChatRoom(roomOptional.get()).stream()
            .map(member -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", member.getUser().getId());
                map.put("nickname", member.getUser().getNickname());
                return map;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(members);
    }
    
 // 채팅방 입장 시 읽음 처리
    @PostMapping("/rooms/{roomId}/read")
    public ResponseEntity<?> markAsRead(@RequestHeader("Authorization") String authHeader,
                                        @PathVariable("roomId") Long roomId) {
        User me = getUserFromToken(authHeader);
        if (me == null) return ResponseEntity.badRequest().body(Map.of("error", "인증 실패"));

        Optional<ChatRoom> roomOptional = chatRoomRepository.findById(roomId);
        if (roomOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "존재하지 않는 채팅방입니다"));
        }

        // 방의 최신 메시지 ID로 갱신
        Message latest = messageRepository.findTopByRoomIdOrderByIdDesc(roomId);
        if (latest != null) {
            Optional<ChatRoomMember> memberOptional =
                chatRoomMemberRepository.findByChatRoomAndUser(roomOptional.get(), me);
            if (memberOptional.isPresent()) {
                ChatRoomMember member = memberOptional.get();
                member.setLastReadMessageId(latest.getId());
                chatRoomMemberRepository.save(member);
            }
        }

        return ResponseEntity.ok(Map.of("message", "읽음 처리 완료"));
    }
    
 // 채팅방 나가기
    @DeleteMapping("/rooms/{roomId}/leave")
    @Transactional
    public ResponseEntity<?> leaveRoom(@RequestHeader("Authorization") String authHeader,
                                       @PathVariable("roomId") Long roomId) {
        User me = getUserFromToken(authHeader);
        if (me == null) return ResponseEntity.badRequest().body(Map.of("error", "인증 실패"));

        Optional<ChatRoom> roomOptional = chatRoomRepository.findById(roomId);
        if (roomOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "존재하지 않는 채팅방입니다"));
        }

        ChatRoom room = roomOptional.get();
        Optional<ChatRoomMember> memberOptional = chatRoomMemberRepository.findByChatRoomAndUser(room, me);

        if (memberOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "이미 나간 채팅방입니다"));
        }

        // 멤버 삭제
        chatRoomMemberRepository.delete(memberOptional.get());

        // 해당 방에 남은 멤버가 없으면 방과 메시지도 삭제
        List<ChatRoomMember> remainingMembers = chatRoomMemberRepository.findByChatRoom(room);
        if (remainingMembers.isEmpty()) {
            messageRepository.deleteByRoomId(roomId);
            chatRoomRepository.delete(room);
        }

        return ResponseEntity.ok(Map.of("message", "채팅방을 나갔습니다"));
    }
    
}