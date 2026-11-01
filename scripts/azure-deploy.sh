#!/usr/bin/env bash
# Sequência reconstruída em 31/10/2026; NÃO foi usada no deploy de 30/10.
# Somente provisionamento. Instalação Docker e deploy manual: docs/DEVOPS.md.
set -Eeuo pipefail
umask 077
trap 'printf "Falha na linha %s. Recursos parciais podem permanecer; nenhuma limpeza automática.\n" "$LINENO" >&2' ERR
fail() { printf '%s\n' "$*" >&2; exit 1; }
usage() {
  cat <<'HELP'
Preparação acadêmica — execução pode gerar custos. Não executar na sessão de documentação.
Uso futuro: configurar variáveis abaixo e executar bash scripts/azure-deploy.sh --apply
Obrigatórias: AZURE_SUBSCRIPTION, SSH_PUBLIC_KEY_FILE, SSH_SOURCE_CIDR (IPv4/32).
O usuário deve autenticar antes com Azure CLI; o script não faz login.
Opcionais: REGION=spaincentral, VM_SIZE=Standard_D2s_v4, RG=rg-nuclear-case,
VNET=vnet-nuclear-case, SUBNET=snet-nuclear-case, NSG=nsg-nuclear-case,
PIP=pip-nuclear-case, NIC=nic-nuclear-case, VM=vm-nuclear-case,
VM_USER=azureuser, HTTP_SOURCE_CIDR=0.0.0.0/0,
VNET_CIDR=10.70.0.0/16, SUBNET_CIDR=10.70.1.0/24,
VM_IMAGE=Canonical:0001-com-ubuntu-server-jammy:22_04-lts-gen2:latest.
Assinatura histórica: Azure for Students; disponibilidade/quota não garantidas.
CIDRs, imagem, disco Standard_LRS/30 GiB e prioridades são escolhas reconstruídas.
HTTP público é temporário. Grupo existente só é aceito se vazio, da região
correta e com tag project=nuclear-case. Grupos ocupados são recusados.
Não há retomada automática, deploy remoto, exclusão ou reinicialização.
HELP
}
case "${1:-}" in
  --help|-h|'') usage; exit 0 ;;
  --apply) [[ $# == 1 ]] || fail 'Argumentos extras não aceitos.' ;;
  *) usage; fail 'Opção inválida.' ;;
esac
for dependency in az python3 ssh-keygen head wc; do
  command -v "$dependency" >/dev/null || fail "Dependência ausente: $dependency (não será instalada)."
done
: "${AZURE_SUBSCRIPTION:?Defina a assinatura privada no terminal.}"
: "${SSH_PUBLIC_KEY_FILE:?Forneça somente o caminho da chave pública.}"
: "${SSH_SOURCE_CIDR:?Defina IPv4 autorizado com /32.}"
REGION=${REGION:-spaincentral}
VM_SIZE=${VM_SIZE:-Standard_D2s_v4}
RG=${RG:-rg-nuclear-case}
VNET=${VNET:-vnet-nuclear-case}
SUBNET=${SUBNET:-snet-nuclear-case}
NSG=${NSG:-nsg-nuclear-case}
PIP=${PIP:-pip-nuclear-case}
NIC=${NIC:-nic-nuclear-case}
VM=${VM:-vm-nuclear-case}
VM_USER=${VM_USER:-azureuser}
HTTP_SOURCE_CIDR=${HTTP_SOURCE_CIDR:-0.0.0.0/0}
VNET_CIDR=${VNET_CIDR:-10.70.0.0/16}
SUBNET_CIDR=${SUBNET_CIDR:-10.70.1.0/24}
VM_IMAGE=${VM_IMAGE:-Canonical:0001-com-ubuntu-server-jammy:22_04-lts-gen2:latest}
for name in "$RG" "$VNET" "$SUBNET" "$NSG" "$PIP" "$NIC" "$VM"; do
  [[ $name =~ ^[a-zA-Z][a-zA-Z0-9-]{0,63}$ ]] || fail 'Nome de recurso inválido.'
done
[[ $VM_USER =~ ^[a-z][a-z0-9_-]{0,31}$ ]] || fail 'Usuário Linux inválido.'
[[ $REGION =~ ^[a-z0-9]+$ && $VM_SIZE =~ ^Standard_[a-zA-Z0-9_]+$ ]] || fail 'Região/SKU inválida.'
[[ $VM_IMAGE =~ ^[a-zA-Z0-9._:-]+$ ]] || fail 'Imagem inválida.'
[[ -r $SSH_PUBLIC_KEY_FILE && $SSH_PUBLIC_KEY_FILE == *.pub ]] || fail 'Chave pública .pub ilegível.'
[[ $(wc -l < "$SSH_PUBLIC_KEY_FILE") -le 1 ]] || fail 'Forneça uma única chave pública.'
[[ $(head -c 12 "$SSH_PUBLIC_KEY_FILE") == ssh-* ]] || fail 'Formato de chave pública inválido.'
ssh-keygen -l -f "$SSH_PUBLIC_KEY_FILE" >/dev/null || fail 'Chave pública inválida.'
python3 - "$SSH_SOURCE_CIDR" "$HTTP_SOURCE_CIDR" "$VNET_CIDR" "$SUBNET_CIDR" <<'PY'
import ipaddress, sys
try:
    ssh, http, vnet, subnet = [ipaddress.IPv4Network(x, strict=True) for x in sys.argv[1:]]
    assert ssh.prefixlen == 32, 'SSH deve estar restrito a IPv4/32'
    assert ssh.network_address.is_global, 'SSH exige endereço público do operador'
    assert subnet.subnet_of(vnet) and subnet != vnet, 'Sub-rede deve estar contida na VNet'
except (ValueError, AssertionError) as exc:
    sys.exit(str(exc))
PY
[[ -t 0 ]] || fail 'Confirmação exige terminal interativo.'
printf 'Criar VM %s / %s / %s no grupo %s; HTTP %s. Pode gerar custos.\n' "$VM" "$REGION" "$VM_SIZE" "$RG" "$HTTP_SOURCE_CIDR"
read -r -p "Digite PROVISIONAR $RG para autorizar: " confirmation
[[ $confirmation == "PROVISIONAR $RG" ]] || fail 'Sem confirmação; nada alterado.'
# Toda chamada fixa a assinatura sem alterar o contexto global da CLI.
azc() { az "$@" --subscription "$AZURE_SUBSCRIPTION" --only-show-errors; }
[[ $(azc account show --query state -o tsv) == Enabled ]] || fail 'Assinatura indisponível; autentique manualmente.'
exists=$(azc group exists --name "$RG" -o tsv)
case "$exists" in
  true)
    [[ $(azc group show -n "$RG" --query location -o tsv) == "$REGION" ]] || fail 'Região do grupo divergente.'
    [[ $(azc group show -n "$RG" --query tags.project -o tsv) == nuclear-case ]] || fail 'Grupo sem identificação esperada.'
    [[ $(azc resource list -g "$RG" --query 'length(@)' -o tsv) == 0 ]] || fail 'Grupo contém recursos; não serão alterados. Revisão manual necessária.'
    ;;
  false) azc group create -n "$RG" -l "$REGION" --tags project=nuclear-case -o none ;;
  *) fail 'Não foi possível determinar se o grupo existe.' ;;
esac
# Grupo comprovadamente vazio: VNet, subnet, NSG, IP, NIC e VM não existem.
azc network vnet create -g "$RG" -n "$VNET" -l "$REGION" \
  --address-prefixes "$VNET_CIDR" --subnet-name "$SUBNET" --subnet-prefixes "$SUBNET_CIDR" -o none
azc network nsg create -g "$RG" -n "$NSG" -l "$REGION" -o none
azc network nsg rule create -g "$RG" --nsg-name "$NSG" -n ssh-operator \
  --priority 100 --direction Inbound --access Allow --protocol Tcp \
  --source-address-prefixes "$SSH_SOURCE_CIDR" --source-port-ranges '*' \
  --destination-address-prefixes '*' --destination-port-ranges 22 -o none
azc network nsg rule create -g "$RG" --nsg-name "$NSG" -n http-demo \
  --priority 110 --direction Inbound --access Allow --protocol Tcp \
  --source-address-prefixes "$HTTP_SOURCE_CIDR" --source-port-ranges '*' \
  --destination-address-prefixes '*' --destination-port-ranges 8080 -o none
azc network vnet subnet update -g "$RG" --vnet-name "$VNET" -n "$SUBNET" --network-security-group "$NSG" -o none
azc network public-ip create -g "$RG" -n "$PIP" -l "$REGION" --sku Standard --version IPv4 --allocation-method Static -o none
azc network nic create -g "$RG" -n "$NIC" -l "$REGION" --vnet-name "$VNET" --subnet "$SUBNET" --public-ip-address "$PIP" -o none
azc vm create -g "$RG" -n "$VM" -l "$REGION" --nics "$NIC" \
  --image "$VM_IMAGE" --size "$VM_SIZE" --admin-username "$VM_USER" \
  --authentication-type ssh --ssh-key-values "$SSH_PUBLIC_KEY_FILE" \
  --storage-sku Standard_LRS --os-disk-size-gb 30 -o none
azc vm show -g "$RG" -n "$VM" --query '{name:name,state:provisioningState,size:hardwareProfile.vmSize}' -o table
printf 'Provisionamento finalizado. Continue manualmente por docs/DEVOPS.md; sem deploy ou exclusão automática.\n'
