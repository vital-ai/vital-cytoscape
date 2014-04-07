package ai.vital.cytoscape.app.internal.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtils {

	public final static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

	public static String convertDate(Long l) {
		return convertDate(new Date(l));
	}
	public static String convertDate(Date date) {
		if(date == null) {
			return "";
		}
		
		return simpleDateFormat.format(date);
		
	}
	
	public static Date convertDate(String dateString) {
		if(dateString != null) {
			if(dateString.equals("") || dateString.equals("null")) {
				return null;
			}
			try {
				return simpleDateFormat.parse(dateString);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		} else {
			return null;
		}
	}
	
}
