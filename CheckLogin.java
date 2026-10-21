import auth.UserDAO;
public class CheckLogin {
    public static void main(String[] args) {
        System.out.println(UserDAO.login("admin@example.com", "admin123") != null ? "login-ok" : "login-failed");
        System.out.println(UserDAO.login("admin@moviebooking.com", "admin123") != null ? "legacy-login-ok" : "legacy-login-failed");
    }
}