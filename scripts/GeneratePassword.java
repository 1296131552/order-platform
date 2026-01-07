
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 生成BCrypt密码哈希
 */
public class GeneratePassword {
    public static void main(String[] args) {
        // 使用与项目相同的strength=10
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);

        // 生成测试密码的哈希
        String adminPassword = "admin123";
        String zhangsanPassword = "123456";

        String adminHash = encoder.encode(adminPassword);
        String zhangsanHash = encoder.encode(zhangsanPassword);

        System.out.println("========================================");
        System.out.println("BCrypt密码哈希生成完成");
        System.out.println("========================================");
        System.out.println();
        System.out.println("用户名: admin");
        System.out.println("密码: " + adminPassword);
        System.out.println("哈希: " + adminHash);
        System.out.println();
        System.out.println("用户名: zhangsan");
        System.out.println("密码: " + zhangsanPassword);
        System.out.println("哈希: " + zhangsanHash);
        System.out.println();
        System.out.println("========================================");

        // 验证哈希是否正确
        System.out.println();
        System.out.println("验证哈希:");
        System.out.println("admin123 匹配? " + encoder.matches(adminPassword, adminHash));
        System.out.println("123456 匹配? " + encoder.matches(zhangsanPassword, zhangsanHash));
        System.out.println("========================================");
    }
}
