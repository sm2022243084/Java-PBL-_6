package pbl_6;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class CalendarDB {

    private JFrame frame;
    private JPanel calendarPanel;
    private JLabel titleLabel;
    private int currentYear;
    private int currentMonth;
    private FoodManage.MainFrame foodManage;
    private ArrayList<Food> foodList;

    public CalendarDB(FoodManage.MainFrame _foodManage) {
        
        // [수정 1] 변수 할당을 가장 먼저 해야 합니다!
        this.foodManage = _foodManage;

        // [수정 2] 이제 this.foodManage가 null이 아니므로 리스트를 잘 가져옵니다.
        if (this.foodManage != null) {
            this.foodList = this.foodManage.getFoodList(); 
        } else {
            this.foodList = new ArrayList<>(); 
        }

        LocalDate today = LocalDate.now();
        currentYear = today.getYear();
        currentMonth = today.getMonthValue();

        frame = new JFrame("📅 유통기한 관리 캘린더");
        frame.setLayout(new BorderLayout());

        // --- 상단 패널 (제목 + 버튼) ---
        JPanel topPanel = new JPanel(new BorderLayout());
        JButton prevButton = new JButton("◀");
        JButton nextButton = new JButton("▶");

        titleLabel = new JLabel(currentYear + "년 " + currentMonth + "월", JLabel.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 22));

        topPanel.add(prevButton, BorderLayout.WEST);
        topPanel.add(titleLabel, BorderLayout.CENTER);
        topPanel.add(nextButton, BorderLayout.EAST);
        frame.add(topPanel, BorderLayout.NORTH);

        // --- 메인 패널 (요일 + 달력) ---
        // [수정 3] 레이아웃이 꼬이지 않도록 중앙 패널을 하나 더 만듭니다.
        JPanel centerPanel = new JPanel(new BorderLayout());

        // 요일 패널
        JPanel dayPanel = new JPanel(new GridLayout(1, 7));
        String[] days = {"일", "월", "화", "수", "목", "금", "토"};
        for (int i = 0; i < days.length; i++) {
            JLabel lbl = new JLabel(days[i], JLabel.CENTER);
            lbl.setFont(new Font("맑은 고딕", Font.BOLD, 14));
            if(i==0) lbl.setForeground(Color.RED); // 일요일 빨강
            else if(i==6) lbl.setForeground(Color.BLUE); // 토요일 파랑
            dayPanel.add(lbl);
        }
        centerPanel.add(dayPanel, BorderLayout.NORTH);

        // 달력 날짜 패널
        calendarPanel = new JPanel(new GridLayout(6, 7, 5, 5));
        centerPanel.add(calendarPanel, BorderLayout.CENTER);

        frame.add(centerPanel, BorderLayout.CENTER);

        // 달력 그리기 실행
        drawCalendar(currentYear, currentMonth);

        // --- 이벤트 ---
        prevButton.addActionListener(e -> {
            currentMonth--;
            if (currentMonth < 1) {
                currentMonth = 12;
                currentYear--;
            }
            drawCalendar(currentYear, currentMonth);
        });

        nextButton.addActionListener(e -> {
            currentMonth++;
            if (currentMonth > 12) {
                currentMonth = 1;
                currentYear++;
            }
            drawCalendar(currentYear, currentMonth);
        });

        frame.setSize(800, 700);
        // [수정 4] 캘린더만 닫히게 설정 (EXIT -> DISPOSE)
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }

    // 📌 해당 날짜에 유통기한이 맞는 음식들 반환
    private List<Food> getFoodsByDate(LocalDate date) {
        List<Food> result = new ArrayList<>();
        for (Food f : foodList) {
            // [수정 5] Null 체크 추가 (유통기한 없는 식품 방지)
            if (f.getExpirationDate() != null && f.getExpirationDate().isEqual(date)) {
                result.add(f);
            }
        }
        return result;
    }

    // 📌 달력 그리기
    private void drawCalendar(int year, int month) {
        calendarPanel.removeAll();
        titleLabel.setText(year + "년 " + month + "월");

        YearMonth yearMonth = YearMonth.of(year, month);
        int daysInMonth = yearMonth.lengthOfMonth();
        LocalDate firstDay = LocalDate.of(year, month, 1);
        int startDay = firstDay.getDayOfWeek().getValue();
        int dayIndex = (startDay == 7 ? 0 : startDay);

        // 빈칸 채우기
        for (int i = 0; i < dayIndex; i++) {
            calendarPanel.add(new JLabel(""));
        }

        // 날짜 채우기
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = LocalDate.of(year, month, day);
            List<Food> todayFoods = getFoodsByDate(date);

            JButton btn = new JButton();
            btn.setLayout(new BorderLayout());

            // 날짜 숫자
            JLabel dateLbl = new JLabel(" " + day);
            dateLbl.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
            btn.add(dateLbl, BorderLayout.NORTH);

            // 색상 및 내용
            if (!todayFoods.isEmpty()) {
                btn.setBackground(determineColor(todayFoods, date));
                
                // 간단 정보 표시
                JLabel info = new JLabel("<html><center>(" + todayFoods.size() + "건)</center></html>", SwingConstants.CENTER);
                info.setFont(new Font("맑은 고딕", Font.BOLD, 11));
                btn.add(info, BorderLayout.CENTER);
            } else {
                btn.setBackground(new Color(230, 240, 255)); // 기본 배경색
            }

            // 클릭 시 팝업
            btn.addActionListener(e -> {
                if (todayFoods.isEmpty()) return;

                StringBuilder sb = new StringBuilder();
                sb.append("📦 ").append(date).append(" 만료 목록\n\n");

                for (Food f : todayFoods) {
                    long dday = ChronoUnit.DAYS.between(LocalDate.now(), f.getExpirationDate());
                    String dStr = (dday == 0) ? "D-Day" : (dday > 0 ? "D-"+dday : "만료");
                    
                    sb.append("- ").append(f.getName())
                      .append(" (").append(dStr).append(")\n");
                }
                JOptionPane.showMessageDialog(frame, sb.toString());
            });

            calendarPanel.add(btn);
        }

        // 남은 빈칸 채우기 (레이아웃 유지용)
        int totalCells = 42; 
        int usedCells = dayIndex + daysInMonth;
        for (int i = usedCells; i < totalCells; i++) {
            calendarPanel.add(new JLabel(""));
        }

        calendarPanel.revalidate();
        calendarPanel.repaint();
    }

    // 📌 색상 결정 로직
    private Color determineColor(List<Food> foods, LocalDate date) {
        if (foods.isEmpty()) return new Color(215, 234, 215);

        long minDday = Long.MAX_VALUE;
        for (Food f : foods) {
            if (f.getExpirationDate() == null) continue;
            long dday = ChronoUnit.DAYS.between(LocalDate.now(), f.getExpirationDate());
            if (dday < minDday) minDday = dday;
        }

        if (minDday < 0) return new Color(255, 102, 102);   // 만료 (빨강)
        if (minDday <= 3) return new Color(255, 180, 90);   // 임박 (주황)
        return new Color(144, 238, 144);                    // 정상 (초록)
    }
}