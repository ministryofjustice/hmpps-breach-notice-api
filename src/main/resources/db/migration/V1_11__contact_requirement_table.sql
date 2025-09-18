CREATE TABLE public.contact_requirement(id uuid not null primary key,
                                              contact_id uuid not null,
                                              requirement_id uuid not null,
                                              breach_notice_id uuid not null,
                                              created_by_user varchar(50) not null,
                                              created_datetime timestamp without time zone NULL,
                                              last_updated_user varchar(50) not null,
                                              last_updated_datetime timestamp without time zone NULL);

ALTER TABLE public.contact_requirement ADD CONSTRAINT xfk1_contact_requirement_contact
    FOREIGN KEY (contact_id) REFERENCES public.breach_notice_contact (id) ON DELETE No Action ON UPDATE No Action;

ALTER TABLE public.contact_requirement ADD CONSTRAINT xfk1_contact_requirement_requirement
    FOREIGN KEY (requirement_id) REFERENCES public.breach_notice_requirement (id) ON DELETE No Action ON UPDATE No Action;

ALTER TABLE public.contact_requirement ADD CONSTRAINT xfk1_contact_requirement_breach_notice
    FOREIGN KEY (breach_notice_id) REFERENCES public.breach_notice (id) ON DELETE No Action ON UPDATE No Action;