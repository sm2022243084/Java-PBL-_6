package pbl_6;
import javax.swing.JFrame;
import java.util.*;
import java.awt.*;
import java.sql.*;
import java.awt.event.*;
import javax.swing.Timer;
import javax.swing.*;
import java.time.*;

public class FoodManage {

    // 1. 메인 프레임 (GUI)
    class MainFrame extends JFrame {
        
        // (1) 메인 프레임이 데이터 리스트를 '소유'
        ArrayList<Food> foodList = new ArrayList<>();
        ConnectJDBC connectJDBC;
        
        public MainFrame() {
            // (2) 프레임이 생성될 때, '자신의 foodList'를
            //     ConnectJDBC에게 넘겨주면서 DB 연결을 시도함.
            connectJDBC = new ConnectJDBC(foodList); 
            
            ActionListener task = new CheckTimeExpiration(this);
            Timer timer = new Timer(3600*1000, task);
            timer.setInitialDelay(0);
            timer.start();
            
            this.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    // X 버튼을 눌렀을 때 실행되는 부분
                    System.out.println("프로그램을 종료합니다. DB 연결을 해제합니다...");
                    
                    // 여기서 우리가 만든 closeConnection()을 호출!
                    connectJDBC.closeConnection(); 
                    
                    // 연결 해제 후 프로그램 진짜 종료
                    System.exit(0); 
                }
            });
        }
        
        public ArrayList<Food> getFoodList() {
            return this.foodList;
        }
        
        
    }

    // 2. DB 연결 클래스
    class ConnectJDBC {
        String url = "jdbc:mysql://localhost:3306/foodmanage?serverTimezone=UTC";
        String id = "root";
        String pw = "";
        Connection connection;
        Statement stmt;
        ResultSet result;
        

        public ConnectJDBC(ArrayList<Food> listToFill) { // (메인의 foodList를 받음)
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(url, id, pw);
                //System.out.println("DB연결완료");
                
                stmt = connection.createStatement();
                result = stmt.executeQuery("select * from food");
                
                while(result.next()) { 
                    Food food = new Food();
                    food.id = result.getString("ID"); 
                    food.name = result.getString("Name"); 
                    food.volume = result.getInt("volume");
                    food.type = result.getString("Type");
                    food.expirationDate = result.getObject("ExpirationDate", LocalDate.class);
                    
                    listToFill.add(food); 
                }
                
                result.close();  
                stmt.close();
                //connection.close(); 현재 닫히는 방식을 windowListener를 통해서 프로그램 종료시 실행방식을 변환시킴
                
            } catch(ClassNotFoundException e) {
                System.out.println("JDBC 드러이버 로드 오류");
                e.printStackTrace();
            } catch(SQLException e) {
                System.out.println("DB 연결 오류");
                e.printStackTrace();
            }	
        }
        public void closeConnection() {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                    System.out.println("DB 연결 종료");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    

    public static void main(String[] args) {
        FoodManage app = new FoodManage(); 
        
        MainFrame frame = app.new MainFrame(); 
        frame.setTitle("식품 관리 프로그램");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}