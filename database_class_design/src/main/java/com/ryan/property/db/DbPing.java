// package com.ryan.property.db;

// import java.sql.Connection;
// import java.sql.ResultSet;
// import java.sql.Statement;

// public class DbPing {
//     public static void main(String[] args) {
//         try (Connection c = DbConnection.open();
//              Statement st = c.createStatement();
//              ResultSet rs = st.executeQuery("SELECT 1")) {

//             rs.next();
//             System.out.println("✅ 数据库连接成功，SELECT 1 = " + rs.getInt(1));

//         } catch (Exception e) {
//             e.printStackTrace();
//         }
//     }
// }
package com.ryan.property.db;

public class DbPing {
    public static void main(String[] args) {
        try (var c = DbConnection.open();
             var st = c.createStatement();
             var rs = st.executeQuery("SELECT DATABASE(), USER(), @@port")) {

            rs.next();
            System.out.println("[DB] database=" + rs.getString(1));
            System.out.println("[DB] user=" + rs.getString(2));
            System.out.println("[DB] port=" + rs.getString(3));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
