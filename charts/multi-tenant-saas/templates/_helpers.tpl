{{- define "multi-tenant-saas.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "multi-tenant-saas.fullname" -}}
{{- if .Values.fullnameOverride -}}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- $name := default .Chart.Name .Values.nameOverride -}}
{{- if contains $name .Release.Name -}}
{{- .Release.Name | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{- define "multi-tenant-saas.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "multi-tenant-saas.labels" -}}
helm.sh/chart: {{ include "multi-tenant-saas.chart" . }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
app.kubernetes.io/part-of: multi-tenant-saas
{{- end -}}

{{- define "multi-tenant-saas.selectorLabels" -}}
app.kubernetes.io/name: {{ include "multi-tenant-saas.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end -}}
