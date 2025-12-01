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
