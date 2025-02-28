package com.xm.util.valid;

import javax.validation.GroupSequence;

public class ValidGroup {
    public interface Insert{}

    public interface Update{}

    public interface Delete{}

    @GroupSequence({Insert.class, Update.class,Delete.class})
    public interface All{}
}
