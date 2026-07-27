{{/*
차트 이름
*/}}
{{- define "realchat.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
전체 이름 (release 기준)
*/}}
{{- define "realchat.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
차트 이름-버전 (라벨용)
*/}}
{{- define "realchat.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
공통 라벨
*/}}
{{- define "realchat.labels" -}}
helm.sh/chart: {{ include "realchat.chart" . }}
{{ include "realchat.selectorLabels" . }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
app.kubernetes.io/part-of: realchat
{{- end }}

{{/*
셀렉터 라벨 — Deployment pod labels 와 Service selector 가 동일해야 함
*/}}
{{- define "realchat.selectorLabels" -}}
app.kubernetes.io/name: {{ include "realchat.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Ingress host — <studentId>-realchat.std.kopoctc.kr 강제
*/}}
{{- define "realchat.host" -}}
{{- if .Values.ingress.host }}
{{- .Values.ingress.host }}
{{- else }}
{{- $id := required "studentId is required (--set studentId=kopo02)" .Values.studentId -}}
{{- printf "%s-%s.std.kopoctc.kr" $id .Values.appName }}
{{- end }}
{{- end }}

{{/*
앱 이미지 풀 경로: <registry>/<repositoryApp>:<tag>
*/}}
{{- define "realchat.image" -}}
{{- printf "%s/%s:%s" .Values.image.registry .Values.image.repositoryApp .Values.image.tag }}
{{- end }}
