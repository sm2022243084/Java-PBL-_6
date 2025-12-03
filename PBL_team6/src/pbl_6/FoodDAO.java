/*
package pbl_6;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FoodDAO {

    // Row → Food 매핑 (ExpDate null 안전)
    private Food mapRow(ResultSet rs) throws SQLException {
        LocalDate exp = null;
        Date d = rs.getDate("ExpirationDate");
        if (d != null) exp = d.toLocalDate();

        Food f = new Food();
        f.id = String.valueOf(rs.getInt("ID"));       // 네 코드가 String id를 쓰고 있어서 맞춤
        f.name = rs.getString("Name");
        f.volume = rs.getInt("volume");
        f.type = rs.getString("Type");
        f.expirationDate = exp;
        return f;
    }

    // CREATE
    public boolean addFood(Food food) {
        String sql = "insert into food (Name, volume, Type, ExpirationDate) values (?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, food.name);
            ps.setInt(2, food.volume);
            ps.setString(3, food.type);
            if (food.expirationDate != null) ps.setDate(4, Date.valueOf(food.expirationDate));
            else                             ps.setNull(4, Types.DATE);

            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            System.err.println("식품 추가 오류: " + e.getMessage());
            return false;
        }
    }

    // READ - 전체
    public List<Food> getAllFoods() {
        List<Food> list = new ArrayList<>();
        String sql = "select * from food order by ExpirationDate asc nulls last";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("전체 조회 오류: " + e.getMessage());
        }
        return list;
    }

    // READ - ID 단건
    public Food getFoodById(int id) {
        String sql = "select * from food where ID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("단건 조회 오류: " + e.getMessage());
        }
        return null;
    }

    // UPDATE
    public boolean updateFood(Food food) {
        String sql = "update food set Name=?, volume=?, Type=?, ExpirationDate=? where ID=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, food.name);
            ps.setInt(2, food.volume);
            ps.setString(3, food.type);
            if (food.expirationDate != null) ps.setDate(4, Date.valueOf(food.expirationDate));
            else                             ps.setNull(4, Types.DATE);
            ps.setInt(5, Integer.parseInt(food.id));

            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            System.err.println("수정 오류: " + e.getMessage());
            return false;
        }
    }

    // DELETE
    public boolean deleteFood(int id) {
        String sql = "delete from food where ID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            System.err.println("삭제 오류: " + e.getMessage());
            return false;
        }
    }
}
*/

package pbl_6;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FoodDAO {

    // 1. 외부에서 받아온 DB 연결 객체를 저장할 변수
    public Connection conn;

    // 2. 생성자: MainFrame(ConnectJDBC)에서 만들어진 Connection을 받아서 저장
    public FoodDAO(Connection conn) {
        this.conn = conn;
    }

    // [헬퍼 메소드] ResultSet의 한 줄을 Food 객체로 변환 (기존 로직 유지)
    private Food mapRow(ResultSet rs) throws SQLException {
        LocalDate exp = null;
        Date d = rs.getDate("ExpirationDate");
        if (d != null) exp = d.toLocalDate();

        Food f = new Food();
        f.id = String.valueOf(rs.getInt("ID"));
        f.name = rs.getString("Name");
        f.volume = rs.getInt("volume");
        f.type = rs.getString("Type");
        f.expirationDate = exp;
        return f;
    }

    // --- CRUD 기능 구현 ---

    // 1. 추가 (CREATE)
    public boolean addFood(Food food) {
        String sql = "INSERT INTO food (Name, volume, Type, ExpirationDate) VALUES (?,?,?,?)";
        
        // [중요] try 안에 'conn'을 넣지 않습니다! (넣으면 여기서 연결이 끊김)
        // 오직 PreparedStatement만 닫아줍니다.
        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, food.name);
            ps.setInt(2, food.volume);
            ps.setString(3, food.type);
            
            // 날짜가 없으면 NULL 처리, 있으면 SQL Date로 변환
            if (food.expirationDate != null) {
                ps.setDate(4, Date.valueOf(food.expirationDate));
            } else {
                ps.setNull(4, Types.DATE);
            }

            // 성공하면 1 반환 -> true
            return ps.executeUpdate() == 1;
            
        } catch (SQLException e) {
            System.err.println("식품 추가 오류: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // 2. 수정 (UPDATE)
    public boolean updateFood(Food food) {
        String sql = "UPDATE food SET Name=?, volume=?, Type=?, ExpirationDate=? WHERE ID=?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, food.name);
            ps.setInt(2, food.volume);
            ps.setString(3, food.type);
            
            if (food.expirationDate != null) {
                ps.setDate(4, Date.valueOf(food.expirationDate));
            } else {
                ps.setNull(4, Types.DATE);
            }
            
            // ID는 WHERE 절에 사용 (int 변환 필요)
            ps.setInt(5, Integer.parseInt(food.id));

            return ps.executeUpdate() == 1;
            
        } catch (SQLException e) {
            System.err.println("수정 오류: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // 3. 삭제 (DELETE)
    public boolean deleteFood(int id) {
        String sql = "DELETE FROM food WHERE ID = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
            
        } catch (SQLException e) {
            System.err.println("삭제 오류: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // 4. 단건 조회 (ID로 찾기 - 수정/삭제 다이얼로그용)
    public Food getFoodById(int id) {
        String sql = "SELECT * FROM food WHERE ID = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("단건 조회 오류: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // 5. [추천] 전체 목록 조회 (MainFrame 테이블 갱신용)
    // 이 메소드가 있으면 MainFrame에서 refreshTable() 만들기가 훨씬 쉬워집니다.
    public ArrayList<Food> getAllFoods() {
        ArrayList<Food> list = new ArrayList<>();
        // 유통기한 순으로 정렬해서 가져오기
        String sql = "SELECT * FROM food ORDER BY ExpirationDate ASC";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                // mapRow 메소드를 활용해 변환
                list.add(mapRow(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("전체 조회 오류: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }
}