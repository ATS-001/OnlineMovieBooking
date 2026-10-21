import java.nio.file.*;
import java.sql.*;
import java.util.*;

public class CheckDbState {
    public static void main(String[] args) throws Exception {
        Map<String,String> env = new HashMap<>();
        for (String line : Files.readAllLines(Path.of(".env"))) {
            String s = line.trim();
            if (s.isEmpty() || s.startsWith("#")) continue;
            int idx = s.indexOf('=');
            if (idx > 0) {
                String key = s.substring(0, idx).trim();
                String value = s.substring(idx + 1).trim();
                if (value.startsWith("\"") && value.endsWith("\"")) value = value.substring(1, value.length()-1);
                env.put(key, value);
            }
        }
        String url = env.get("DB_URL");
        String user = env.get("DB_USER");
        String pass = env.get("DB_PASSWORD");
        System.out.println("URL=" + url);
        System.out.println("USER=" + user);
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            try (PreparedStatement ps = conn.prepareStatement("SELECT user_id, name, email, password, role FROM Users ORDER BY user_id")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        System.out.println(rs.getInt("user_id") + " | " + rs.getString("name") + " | " + rs.getString("email") + " | " + rs.getString("role"));
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}