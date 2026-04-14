alter table if exists team.team_invitations
    add column if not exists invitee_user_id bigint;

alter table if exists team.team_invitations
    add column if not exists invitee_login_id varchar(50);

alter table if exists team.team_invitations
    add column if not exists invitee_display_name varchar(80);

alter table if exists team.team_invitations
    add column if not exists rejected boolean default false;
