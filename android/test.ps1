param([string]$Jdk='')
# Compatibility entry point; both native apps use the same core tests.
& (Join-Path (Split-Path -Parent $PSScriptRoot) 'core/test.ps1') -Jdk $Jdk
