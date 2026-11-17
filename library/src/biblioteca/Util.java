package biblioteca;

import java.text.Normalizer;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/** funções auxiliares */
public final class Util {
    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private Util() {
    }

    public static LocalDate paraLocal(Date data) {
        return data.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /** cria data */
    public static Date criar(int ano, int mes, int dia) {
        try {
            return Date.from(LocalDate.of(ano, mes, dia).atStartOfDay(ZoneId.systemDefault()).toInstant());
        } catch (DateTimeException e) {
            throw new IllegalArgumentException("Data inválida: " + dia + "/" + mes + "/" + ano);
        }
    }

    public static Date somarDias(Date data, int dias) {
        Calendar c = Calendar.getInstance();
        c.setTime(data);
        c.add(Calendar.DAY_OF_MONTH, dias);
        return c.getTime();
    }

    /** dias entre datas */
    public static long diasEntre(Date inicio, Date fim) {
        return ChronoUnit.DAYS.between(paraLocal(inicio), paraLocal(fim));
    }

    public static String formatar(Date data) {
        return data == null ? "-" : FORMATO.format(paraLocal(data));
    }

    public static Date interpretar(String texto) throws BibliotecaException {
        try {
            LocalDate d = LocalDate.parse(texto.trim(), FORMATO);
            return Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant());
        } catch (DateTimeParseException e) {
            throw new BibliotecaException("Data inválida: \"" + texto + "\". Use o formato dd/MM/aaaa.");
        }
    }

    public static String moeda(double valor) {
        return String.format(PT_BR, "R$ %,.2f", valor);
    }

    public static boolean vazio(String s) {
        return s == null || s.trim().isEmpty();
    }

    /** minúsculas sem acento */
    public static String normalizar(String s) {
        if (s == null) {
            return "";
        }
        String n = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return n.toLowerCase(PT_BR).trim();
    }

    /** chave de CPF/matrícula */
    public static String chave(String s) {
        return s == null ? "" : s.replaceAll("[.\\-/\\s]", "").toLowerCase(PT_BR);
    }
}
