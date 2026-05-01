ALTER TABLE public.breach_notice ADD COLUMN alternate_next_appointment_location uuid NULL;
ALTER TABLE public.breach_notice ADD COLUMN alternate_next_appointment_location_selected boolean NULL;
UPDATE public.breach_notice SET alternate_next_appointment_location_selected = false where alternate_next_appointment_location_selected is NULL;
ALTER TABLE public.breach_notice ADD CONSTRAINT xfk2_breach_alt_address
    FOREIGN KEY (alternate_next_appointment_location) REFERENCES public.address (id) ON DELETE No Action ON UPDATE No Action;
