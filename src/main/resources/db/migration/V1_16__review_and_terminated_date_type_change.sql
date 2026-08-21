ALTER TABLE public.breach_notice ALTER column review_required_date type timestamp with time zone USING review_required_date AT TIME ZONE 'Europe/London',
ALTER TABLE public.breach_notice ALTER column terminated_unterminated_date type timestamp with time zone USING terminated_unterminated_date AT TIME ZONE 'Europe/London';
