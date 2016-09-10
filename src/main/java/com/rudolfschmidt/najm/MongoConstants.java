package com.rudolfschmidt.najm;

import java.time.LocalTime;
import java.util.Date;

public interface MongoConstants {
	String ID = "_id";
	String FIND_BY = "findBy";
	Class[] CODEC_TYPES = {String.class, Boolean.class, Date.class};
	Class[] STRING_TYPES = {LocalTime.class};
}
