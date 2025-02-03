CREATE TABLE public.breach_notice(id uuid not null primary key,
                           crn char(7) not null,
                           title_and_full_name varchar(200) NULL,
                           date_of_letter timestamp without time zone NULL,
                           reference_number varchar(50) NULL,
                           response_required_date timestamp without time zone NULL,
                           breach_notice_type_code varchar(50) NULL,
                           breach_notice_type_description varchar(200) NULL,
                           breach_sentence_type_code varchar(50) NULL,
                           breach_condition_type_code varchar(50) NULL,
                           responsible_officer varchar(100) NULL,
                           contact_number varchar(35) NULL,
                           next_appointment_type varchar(50) NULL,
                           next_appointment_date timestamp without time zone NULL,
                           next_appointment_location varchar(100) NULL,
                           next_appointment_officer varchar(100) NULL,
                           next_appointment_contact_id uuid NULL,
                           completed_date timestamp without time zone NULL,
                           created_by_user varchar(100) not NULL,
                           created_datetime timestamp without time zone not NULL,
                           last_updated_datetime timestamp without time zone not NULL,
                           last_updated_user varchar(100) not NULL,
                           offender_address_id uuid NULL,
                           reply_address_id uuid NULL,
                           basic_details_saved boolean NULL,
                           warning_type_saved boolean NULL,
                           warning_details_saved boolean NULL,
                           next_appointment_saved boolean NULL,
                           use_default_address boolean NULL,
                           use_default_reply_address boolean NULL);

CREATE TABLE public.address(id uuid not null primary Key,
                     address_id bigint not null,
                     type varchar(100) NULL,
                     building_name varchar(35) NULL,
                     address_number varchar(35) NULL,
                     street_name varchar(35) NULL,
                     district varchar(35) NULL,
                     town_city varchar(35) NULL,
                     county varchar(35) NULL,
                     postcode varchar(8) NULL,
                     created_by_user varchar(50) not null,
                     created_datetime timestamp without time zone not null,
                     last_updated_user varchar(50) not null,
                     last_updated_datetime timestamp without time zone not null);

CREATE TABLE public.breach_notice_contact(id uuid not null primary key,
                                          breach_notice_id uuid not null ,
                                          contact_date timestamp without time zone not null,
                                          contact_type varchar(100) NULL,
                                          contact_outcome varchar(100) NULL,
                                          contact_id bigint not null,
                                          created_by_user varchar(50) not null,
                                          created_datetime timestamp without time zone NULL,
                                          last_updated_user varchar(50) not null,
                                          last_updated_datetime timestamp without time zone NULL);

CREATE TABLE public.breach_notice_requirement(id uuid not null primary key,
                                          breach_notice_id uuid not null,
                                          requirement_id bigint not null,
                                          requirement_type_main_category_description varchar(200) NULL,
                                          requirement_type_sub_category_description varchar(200) NULL,
                                          rejection_reason varchar(200) NULL,
                                          created_by_user varchar(50) not null,
                                          created_datetime timestamp without time zone NULL,
                                          last_updated_user varchar(50) not null,
                                          last_updated_datetime timestamp without time zone NULL);

ALTER TABLE public.breach_notice_requirement ADD CONSTRAINT xfk1_breach_notice_requirement
    FOREIGN KEY (breach_notice_id) REFERENCES public.breach_notice (id) ON DELETE No Action ON UPDATE No Action;

ALTER TABLE public.breach_notice_contact ADD CONSTRAINT xfk1_breach_notice_contact
    FOREIGN KEY (breach_notice_id) REFERENCES public.breach_notice (id) ON DELETE No Action ON UPDATE No Action;

ALTER TABLE public.breach_notice ADD CONSTRAINT xfk1_breach_notice_offender_address
    FOREIGN KEY (offender_address_id) REFERENCES public.address (id) ON DELETE No Action ON UPDATE No Action;

ALTER TABLE public.breach_notice ADD CONSTRAINT xfk1_breach_notice_reply_address
    FOREIGN KEY (reply_address_id) REFERENCES public.address (id) ON DELETE No Action ON UPDATE No Action;

ALTER TABLE public.breach_notice ADD CONSTRAINT xfk1_breach_notice_contact
    FOREIGN KEY (next_appointment_contact_id) REFERENCES public.breach_notice_contact (id) ON DELETE No Action ON UPDATE No Action;
