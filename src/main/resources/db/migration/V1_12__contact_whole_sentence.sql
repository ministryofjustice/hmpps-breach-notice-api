ALTER TABLE public.breach_notice_contact ADD COLUMN rejection_reason varchar(200) NULL;
ALTER TABLE public.breach_notice_contact ADD COLUMN whole_sentence boolean NULL;
UPDATE public.breach_notice_contact SET whole_sentence = false where whole_sentence is NULL;