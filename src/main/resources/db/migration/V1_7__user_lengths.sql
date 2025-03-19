ALTER TABLE public.address ALTER COLUMN created_by_user TYPE varchar (100);
ALTER TABLE public.address ALTER COLUMN last_updated_user TYPE varchar (100);
ALTER TABLE public.breach_notice_contact ALTER COLUMN created_by_user TYPE varchar (100);
ALTER TABLE public.breach_notice_contact ALTER COLUMN last_updated_user TYPE varchar (100);
ALTER TABLE public.breach_notice_requirement ALTER COLUMN created_by_user TYPE varchar (100);
ALTER TABLE public.breach_notice_requirement ALTER COLUMN last_updated_user TYPE varchar (100);
ALTER TABLE public.breach_notice ALTER COLUMN next_appointment_id TYPE bigint;