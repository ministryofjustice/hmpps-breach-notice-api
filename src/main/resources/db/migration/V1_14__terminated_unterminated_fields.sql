ALTER TABLE public.breach_notice ADD COLUMN terminated boolean NOT NULL DEFAULT false;
UPDATE public.breach_notice SET terminated = false WHERE terminated IS NULL;
ALTER TABLE public.breach_notice ADD COLUMN terminated_unterminated_date timestamp NULL;
