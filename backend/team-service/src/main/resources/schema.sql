alter table if exists team.team_invitations
    add column if not exists invitee_user_id bigint;

alter table if exists team.team_invitations
    add column if not exists invitee_login_id varchar(50);

alter table if exists team.team_invitations
    add column if not exists invitee_display_name varchar(80);

alter table if exists team.team_invitations
    add column if not exists rejected boolean default false;

update team.team_invitations invitation
set invitee_user_id = users.id,
    invitee_login_id = users.login_id,
    invitee_display_name = users.display_name
from auth.users users
where lower(invitation.invitee_email) = lower(users.email)
  and (
      invitation.invitee_user_id is null
      or invitation.invitee_login_id is null
      or invitation.invitee_display_name is null
  );

update team.team_invitations
set rejected = false
where rejected is null;
