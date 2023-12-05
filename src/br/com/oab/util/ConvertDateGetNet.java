package br.com.oab.util;


import java.util.Locale;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class ConvertDateGetNet {

    public static DateTime formatDateStringGetNetFormatYYYYmmDDThhMMSS(String inputDateString) {
        // Exemplo de Data : "2023-10-25T05:11:00.856Z";
        LocalDateTime localDateTime = LocalDateTime.parse(inputDateString, DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
        String formattedDateTime = formatter.print(localDateTime);
        return formatter.parseDateTime(formattedDateTime);
    }

	public static DateTime convertDateTimeDiaExMesExDDHHMMSSS(String inputDateString) {
		// Exemplo "Tue Oct 24 08:00:21 BRT 2023";

		DateTimeFormatter outputFormatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");

		DateTimeFormatter inputFormatter = DateTimeFormat.forPattern("EEE MMM dd HH:mm:ss 'BRT' yyyy")
				.withLocale(Locale.ENGLISH);
		DateTime dateTime = inputFormatter.parseDateTime(inputDateString);

		String formattedDateTime = outputFormatter.print(dateTime);

		return outputFormatter.parseDateTime(formattedDateTime);

	}
}
