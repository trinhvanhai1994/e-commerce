#!/usr/bin/env bash
# Kiểm tài khoản git trước khi commit hoặc push repo e-commerce.
# Repo này chỉ được commit/push bằng email REQUIRED_EMAIL (owner chốt 2026-11-18).
#
# Cách dùng:
#   check-git-account.sh              kiểm identity commit + tài khoản push, in kết quả
#   check-git-account.sh --hook       chạy như PreToolUse hook của Claude Code: đọc JSON lệnh từ stdin,
#                                     chỉ kiểm khi lệnh có git commit / git push / git-open-pr,
#                                     sai thì exit 2 để chặn lệnh
set -u

REQUIRED_EMAIL="trinhhai28041994@gmail.com"
REQUIRED_GH_USER="trinhvanhai1994"

mode="manual"
action="all"
if [ "${1:-}" = "--hook" ]; then
  mode="hook"
  input="$(cat)"
  # Chỉ quan tâm lệnh git commit / git push / script mở PR
  if printf '%s' "$input" | grep -Eq 'git([[:space:]]+-[^[:space:]]+([[:space:]]+[^-[:space:]][^[:space:]]*)?)*[[:space:]]+push|git-open-pr'; then
    action="push"
  elif printf '%s' "$input" | grep -Eq 'git([[:space:]]+-[^[:space:]]+([[:space:]]+[^-[:space:]][^[:space:]]*)?)*[[:space:]]+commit'; then
    action="commit"
  else
    exit 0
  fi
fi

repo_root="$(git rev-parse --show-toplevel 2>/dev/null)" || exit 0
cd "$repo_root" || exit 0

errors=()

email="$(git config user.email || true)"
name="$(git config user.name || true)"
if [ "$email" != "$REQUIRED_EMAIL" ]; then
  errors+=("Email commit đang là '${email:-<trống>}', phải là '$REQUIRED_EMAIL'. Sửa: git config --local user.email $REQUIRED_EMAIL")
fi
if [ -z "$name" ]; then
  errors+=("Chưa có user.name. Sửa: git config --local user.name <tên>")
fi

if [ "$action" = "push" ] || [ "$action" = "all" ]; then
  gh_user="$(git config --get credential.https://github.com.username || true)"
  if [ "$gh_user" != "$REQUIRED_GH_USER" ]; then
    errors+=("Tài khoản push GitHub đang là '${gh_user:-<không ghim>}', phải là '$REQUIRED_GH_USER'. Sửa: git config --local credential.https://github.com.username $REQUIRED_GH_USER")
  fi
  # Commit chưa có trên remote nào mà mang email khác sẽ bị chặn push
  bad="$(git log HEAD --not --remotes --format='%h %ae | %ce' 2>/dev/null \
        | grep -v "^[^ ]* $REQUIRED_EMAIL | $REQUIRED_EMAIL\$" || true)"
  if [ -n "$bad" ]; then
    errors+=("Có commit chưa push mang email khác '$REQUIRED_EMAIL' (author | committer):
$bad
Hỏi owner trước khi sửa lại các commit này; không tự viết lại lịch sử.")
  fi
fi

if [ ${#errors[@]} -eq 0 ]; then
  [ "$mode" = "manual" ] && echo "GIT_ACCOUNT=ok ($name <$email>, push: $REQUIRED_GH_USER)"
  exit 0
fi

{
  echo "GIT_ACCOUNT=wrong: repo e-commerce chỉ được commit/push bằng $REQUIRED_EMAIL."
  for e in "${errors[@]}"; do echo "- $e"; done
} >&2
[ "$mode" = "hook" ] && exit 2
exit 1
