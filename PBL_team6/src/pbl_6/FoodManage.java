
package pbl_6;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.List;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.DriverManager; // 필요 시 import
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.*;
import java.util.ArrayList;
import javax.swing.Timer;
import java.time.LocalDate;

public class FoodManage {

    class MainFrame extends JFrame {
        
        // 데이터 관리용 변수
        ConnectJDBC connectJDBC; // DB 연결 관리자
        FoodDAO foodDAO;         // DB 작업 일꾼
        ArrayList<Food> foodList;
        
        // 화면 표시용 변수 (JTable)
        JTable table;
        DefaultTableModel tableModel;
        String[] colNames = {"ID", "식품명", "종류", "수량", "유통기한"};

        public MainFrame() {
            // 1. DB 연결 및 DAO 초기화
            foodList = new ArrayList<>();
            connectJDBC = new ConnectJDBC(foodList); // DB 연결 시도
            
            // [중요] 연결된 Connection을 DAO에게 넘겨줍니다.
            Connection conn = connectJDBC.getConnection();
            foodDAO = new FoodDAO(conn);

            // 2. GUI 설정
            setTitle("식품 관리 프로그램");
            setSize(600, 400);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLayout(new BorderLayout());

            // 3. 테이블(목록) 만들기
            tableModel = new DefaultTableModel(colNames, 0) {
                // 셀 수정 불가하게 설정 (오직 다이얼로그로만 수정)
                public boolean isCellEditable(int row, int column) { return false; }
            };
            table = new JTable(tableModel);
            JScrollPane scrollPane = new JScrollPane(table);
            add(scrollPane, BorderLayout.CENTER);

            // 4. 버튼 패널 만들기 (남쪽에 배치)
            JPanel btnPanel = new JPanel();
            JButton btnAdd = new JButton("추가");
            JButton btnEdit = new JButton("수정");
            JButton btnDel = new JButton("삭제");
            JButton btnCal = new JButton("캘린더"); // 캘린더 버튼 추가

            btnPanel.add(btnAdd);
            btnPanel.add(btnEdit);
            btnPanel.add(btnDel);
            btnPanel.add(btnCal);
            add(btnPanel, BorderLayout.SOUTH);

            // --- [버튼 이벤트 연결] ---

            // [추가 버튼]
            btnAdd.addActionListener(e -> {
                // AddFoodDialog를 띄웁니다.
                // 마지막 인자 () -> refreshTable() 은 "작업 성공 시 목록 새로고침 해라"라는 명령입니다.
                new AddFoodDialog(this, foodDAO, () -> refreshTable()).setVisible(true);
            });

            // [수정 버튼]
            btnEdit.addActionListener(e -> {
                int row = table.getSelectedRow(); // 테이블에서 선택된 줄 번호
                if (row == -1) {
                    JOptionPane.showMessageDialog(this, "수정할 식품을 선택하세요.");
                    return;
                }
                
                // 선택된 줄의 ID 가져오기 (테이블 0번째 컬럼에 ID가 있다고 가정)
                String idStr = (String) tableModel.getValueAt(row, 0);
                int id = Integer.parseInt(idStr);

                // EditFoodDialog 띄우기
                new EditFoodDialog(this, foodDAO, id, () -> refreshTable()).setVisible(true);
            });

            // [삭제 버튼]
            btnDel.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row == -1) {
                    JOptionPane.showMessageDialog(this, "삭제할 식품을 선택하세요.");
                    return;
                }
                String idStr = (String) tableModel.getValueAt(row, 0);
                int id = Integer.parseInt(idStr);

                // DeleteFoodDialog 띄우기
                new DeleteFoodDialog(this, foodDAO, id, () -> refreshTable()).setVisible(true);
            });
            
            // [캘린더 버튼]
            btnCal.addActionListener(e -> {
                 // 캘린더는 현재 리스트(foodList)가 필요하므로 this를 넘김
                 new CalendarDB(this);
            });

            // 5. 프로그램 켜지면 데이터 한번 불러오기
            refreshTable();
            
            // 6. 타이머 시작 (유통기한 자동 체크)
            ActionListener task = new CheckTimeExpiration(this);
            Timer timer = new Timer(3600*1000, task);
            timer.setInitialDelay(0);
            timer.start();

            // 7. 종료 시 DB 연결 해제
            this.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    connectJDBC.closeConnection();
                }
            });
            
            setVisible(true);
        }
        
        // [핵심 기능] DB에서 데이터를 다시 가져와서 테이블과 리스트를 갱신하는 메소드
        public void refreshTable() {
            // 1. 기존 화면과 리스트를 싹 비웁니다.
            tableModel.setRowCount(0);
            foodList.clear(); // [핵심] 캘린더가 보는 리스트도 비워줍니다.

            // 2. DAO를 통해 DB에서 최신 데이터를 몽땅 가져옵니다.
            ArrayList<Food> allData = foodDAO.getAllFoods();

            // 3. 가져온 데이터를 화면과 리스트 양쪽에 채워 넣습니다.
            for (Food f : allData) {
                // (1) 화면(JTable)에 추가
                tableModel.addRow(new Object[]{
                    f.id, 
                    f.name, 
                    f.type, 
                    f.volume, 
                    f.expirationDate
                });
                
                // (2) [핵심] 캘린더가 사용하는 메모리 리스트에도 추가!!
                // 이걸 안 하면 캘린더에는 아무것도 안 뜹니다.
                foodList.add(f);
            }
        }

        // 다른 클래스(알림, 캘린더)에서 리스트가 필요할 때 호출
        public ArrayList<Food> getFoodList() {
            return this.foodList;
        }
    }

    // ConnectJDBC 클래스 (수정된 버전)
    class ConnectJDBC {
        Connection connection; 
        String url = "jdbc:mysql://localhost:3306/foodmanage?serverTimezone=UTC";
        String id = "root";
        String pw = "";
        
        Statement stmt;
        ResultSet result;
        
        public ConnectJDBC(ArrayList<Food> list) {
        	try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(url, id, pw);
                System.out.println("DB연결완료");
                
                stmt = connection.createStatement();
                result = stmt.executeQuery("select * from food");
                
                while(result.next()) { 
                    Food food = new Food();
                    food.id = result.getString("ID"); 
                    food.name = result.getString("Name"); 
                    food.volume = result.getInt("volume");
                    food.type = result.getString("Type");
                    food.expirationDate = result.getObject("ExpirationDate", LocalDate.class);
                    
                    list.add(food); 
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
        public Connection getConnection() {
        	return connection; 
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
        app.new MainFrame();
    }
}