//package pbl_6.calendar;

import javax.swing.*;

import pbl_6.FoodManage;

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
    private FoodManage foodManage
    private ArrayList<Food> foodList;

    public CalendarDB(FoodManage.MainFrame _foodManage) {

    	if (foodManage != null) {
            this.foodList = foodManage.getFoodList(); 
        } else {
            this.foodList = new ArrayList<>(); // 방어 코드
        }

        LocalDate today = LocalDate.now();
        currentYear = today.getYear();
        currentMonth = today.getMonthValue();

        frame = new JFrame("📅 유통기한 관리 캘린더");
        frame.setLayout(new BorderLayout());

        // 상단 패널
        JPanel topPanel = new JPanel(new BorderLayout());
        JButton prevButton = new JButton("◀");
        JButton nextButton = new JButton("▶");

        titleLabel = new JLabel(currentYear + "년 " + currentMonth + "월", JLabel.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 22));

        topPanel.add(prevButton, BorderLayout.WEST);
        topPanel.add(titleLabel, BorderLayout.CENTER);
        topPanel.add(nextButton, BorderLayout.EAST);
        frame.add(topPanel, BorderLayout.NORTH);

        // 요일 패널
        JPanel dayPanel = new JPanel(new GridLayout(1, 7));
        String[] days = {"일", "월", "화", "수", "목", "금", "토"};
        for (String d : days) {
            JLabel lbl = new JLabel(d, JLabel.CENTER);
            lbl.setFont(new Font("맑은 고딕", Font.BOLD, 14));
            dayPanel.add(lbl);
        }
        frame.add(dayPanel, BorderLayout.CENTER);

        // 달력 패널
        calendarPanel = new JPanel(new GridLayout(6, 7, 5, 5));
        frame.add(calendarPanel, BorderLayout.SOUTH);

        drawCalendar(currentYear, currentMonth);

        // 달 이동
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

        frame.setSize(700, 550);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    // 📌 해당 날짜에 유통기한이 맞는 음식들 반환
    private List<Food> getFoodsByDate(LocalDate date) {
        List<Food> result = new ArrayList<>();

        for (Food f : foodList) {
            if (f.getExpireDate().equals(date)) {
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

            JButton btn = new JButton(String.valueOf(day));

            // 🔥 색상 적용 (DB 기반)
            btn.setBackground(determineColor(todayFoods, date));

            // 🔥 클릭 시 팝업
            btn.addActionListener(e -> {
                List<Food> list = getFoodsByDate(date);

                if (list.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "등록된 식품 없음");
                    return;
                }

                StringBuilder sb = new StringBuilder();
                sb.append("📦 ").append(date).append(" 유통기한 목록\n\n");

                for (Food f : list) {
                    long dday = ChronoUnit.DAYS.between(LocalDate.now(), f.getExpireDate());
                    sb.append("- ").append(f.getName())
                      .append(" (D-").append(dday).append(")\n");
                }

                JOptionPane.showMessageDialog(frame, sb.toString());
            });

            calendarPanel.add(btn);
        }

        calendarPanel.revalidate();
        calendarPanel.repaint();
    }

    // 📌 색상 결정 로직 (DB 연동OK)
    private Color determineColor(List<Food> foods, LocalDate date) {

        if (foods.isEmpty()) return new Color(215, 234, 215); // 기본색

        // 임박 / 만료 기준 중 가장 촉박한 것을 기준으로 결정
        long minDday = Long.MAX_VALUE;

        for (Food f : foods) {
            long dday = ChronoUnit.DAYS.between(LocalDate.now(), f.getExpireDate());
            if (dday < minDday) {
                minDday = dday;
            }
        }

        if (minDday < 0) return new Color(255, 102, 102);   // 만료 (빨강)
        if (minDday <= 3) return new Color(255, 180, 90);   // 임박 (주황)
        return new Color(144, 238, 144);                    // 정상 (초록)
    }

    // 🔥 테스트용 실행 (DB 연동 전에도 동작됨)
    public static void main(String[] args) {

        // DB 연결 전 임시 테스트 데이터 (추후 삭제)
        List<Food> testFoods = new ArrayList<>();
        testFoods.add(new Food("우유", LocalDate.now().plusDays(2)));
        testFoods.add(new Food("계란", LocalDate.now().plusDays(5)));
        testFoods.add(new Food("김치", LocalDate.now().minusDays(1)));

        new CalendarDB(testFoods);
    }
}