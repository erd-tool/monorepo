{{- define "erd-tool.name" -}}
erd-tool
{{- end -}}

{{- define "erd-tool.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" -}}
{{- end -}}

{{- define "erd-tool.labels" -}}
helm.sh/chart: {{ include "erd-tool.chart" . }}
app.kubernetes.io/name: {{ include "erd-tool.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
app.kubernetes.io/part-of: erd-tool
{{- end -}}

{{- define "erd-tool.selectorLabels" -}}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/part-of: erd-tool
{{- end -}}

{{- define "erd-tool.componentLabels" -}}
{{- $root := index . 0 -}}
{{- $name := index . 1 -}}
{{ include "erd-tool.labels" $root }}
app.kubernetes.io/component: {{ $name }}
{{- end -}}

{{- define "erd-tool.componentSelectorLabels" -}}
{{- $root := index . 0 -}}
{{- $name := index . 1 -}}
{{ include "erd-tool.selectorLabels" $root }}
app.kubernetes.io/component: {{ $name }}
{{- end -}}

{{- define "erd-tool.appImage" -}}
{{- $root := index . 0 -}}
{{- $name := index . 1 -}}
{{ printf "%s/%s/%s:%s" $root.Values.global.imageRegistry $root.Values.global.imageRepository $name $root.Values.global.imageTag }}
{{- end -}}

{{- define "erd-tool.externalOrigin" -}}
{{- $scheme := .Values.global.domainScheme | default "https" -}}
{{- $host := .Values.global.host -}}
{{- $port := printf "%v" (.Values.global.externalPort | default "") -}}
{{- $defaultPort := "80" -}}
{{- if eq $scheme "https" -}}
{{- $defaultPort = "443" -}}
{{- end -}}
{{- if and $port (ne $port "") (ne $port $defaultPort) -}}
{{ printf "%s://%s:%s" $scheme $host $port }}
{{- else -}}
{{ printf "%s://%s" $scheme $host }}
{{- end -}}
{{- end -}}

{{- define "erd-tool.useCustomHttpsPort" -}}
{{- $scheme := .Values.global.domainScheme | default "https" -}}
{{- $port := printf "%v" (.Values.global.externalPort | default "") -}}
{{- if and (eq $scheme "https") (ne $port "") (ne $port "443") -}}
true
{{- end -}}
{{- end -}}
