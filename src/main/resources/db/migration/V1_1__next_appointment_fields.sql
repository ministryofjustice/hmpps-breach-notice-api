ALTER TABLE public.breach_notice DROP CONSTRAINT xfk1_breach_notice_contact;
ALTER TABLE public.breach_notice DROP COLUMN next_appointment_contact_id;
ALTER TABLE public.breach_notice ADD optional_number_checked boolean NULL;
ALTER TABLE public.breach_notice ADD optional_number varchar(255) NULL;
ALTER TABLE public.breach_notice ADD next_appointment_id integer NULL;

