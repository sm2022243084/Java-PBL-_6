package pbl_6.calendar;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;

public class calendarr {
    private JFrame frame;
    private JPanel calendarPanel;
    private JLabel titleLabel;
    private int currentYear;
    private int currentMonth;

    public calendarr() {
        LocalDate today = LocalDate.now();
        currentYear = today.getYear();
        currentMonth = today.getMonthValue();

        frame = new JFrame("📅 유통기한 관리 캘린더");
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(new Color(245, 247, 250));

        // 상단: 제목 + 이전/다음 버튼
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(230, 235, 245));

        JButton prevButton = new JButton("◀");
        JButton nextButton = new JButton("▶");

        styleButton(prevButton);
        styleButton(nextButton);

        titleLabel = new JLabel(currentYear + "년 " + currentMonth + "월", JLabel.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 22));

        topPanel.add(prevButton, BorderLayout.WEST);
        topPanel.add(titleLabel, BorderLayout.CENTER);
        topPanel.add(nextButton, BorderLayout.EAST);
        frame.add(topPanel, BorderLayout.NORTH);

        // 메인 컨테이너 (요일 + 달력)
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        // 요일 표시
        JPanel dayPanel = new JPanel(new GridLayout(1, 7));
        String[] days = {"일", "월", "화", "수", "목", "금", "토"};

        for (int i = 0; i < days.length; i++) {
            JLabel dayLabel = new JLabel(days[i], JLabel.CENTER);
            dayLabel.setFont(new Font("맑은 고딕", Font.BOLD, 15));
            if (i == 0) dayLabel.setForeground(Color.RED);
            else if (i == 6) dayLabel.setForeground(Color.BLUE);
            dayPanel.add(dayLabel);
        }
        mainPanel.add(dayPanel, BorderLayout.NORTH);

        // 달력 날짜 패널
        calendarPanel = new JPanel(new GridLayout(6, 7, 5, 5));
        calendarPanel.setBackground(Color.WHITE);
        mainPanel.add(calendarPanel, BorderLayout.CENTER);

        frame.add(mainPanel, BorderLayout.CENTER);

        // 하단 범례
        JPanel legendPanel = new JPanel();
        legendPanel.setBackground(new Color(240, 242, 245));
        legendPanel.add(makeLegend(Color.GREEN, "정상"));
        legendPanel.add(makeLegend(Color.ORANGE, "임박"));
        legendPanel.add(makeLegend(Color.RED, "만료"));
        frame.add(legendPanel, BorderLayout.SOUTH);

        // 초기 달력 표시
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

        frame.setSize(750, 650);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private void drawCalendar(int year, int month) {
        calendarPanel.removeAll();
        titleLabel.setText(year + "년 " + month + "월");

        YearMonth yearMonth = YearMonth.of(year, month);
        int daysInMonth = yearMonth.lengthOfMonth();
        LocalDate firstDay = LocalDate.of(year, month, 1);
        int startDay = firstDay.getDayOfWeek().getValue(); // 월(1)~일(7)
        int dayIndex = (startDay == 7) ? 0 : startDay;

        // 빈칸
        for (int i = 0; i < dayIndex; i++) {
            calendarPanel.add(new JLabel(""));
        }

        // 날짜 버튼
        for (int day = 1; day <= daysInMonth; day++) {
            JButton dayButton = new JButton(String.valueOf(day));
            dayButton.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
            dayButton.setOpaque(true);
            dayButton.setBorderPainted(false);

            // 색상 구분
            if (day % 7 == 0) dayButton.setBackground(new Color(255, 102, 102)); // 만료
            else if (day % 5 == 0) dayButton.setBackground(new Color(255, 180, 90)); // 임박
            else dayButton.setBackground(new Color(144, 238, 144)); // 정상

            // 클릭 시 팝업
            dayButton.addActionListener(e -> {
                String msg = "📦 " + dayButton.getText() + "일 제품 목록\n" +
                        "- 우유 (D-2)\n" +
                        "- 김치 (D-5)\n" +
                        "- 계란 (D-8)";
                JOptionPane.showMessageDialog(frame, msg, "제품 상세보기", JOptionPane.INFORMATION_MESSAGE);
            });

            calendarPanel.add(dayButton);
        }

        // 나머지 빈칸 채우기
        int totalCells = 42; // 6행*7열
        int usedCells = dayIndex + daysInMonth;
        for (int i = usedCells; i < totalCells; i++) {
            calendarPanel.add(new JLabel(""));
        }

        calendarPanel.revalidate();
        calendarPanel.repaint();
    }

    // 버튼 스타일
    private void styleButton(JButton btn) {
        btn.setBackground(new Color(220, 230, 250));
        btn.setFocusPainted(false);
        btn.setFont(new Font("맑은 고딕", Font.BOLD, 16));
    }

    private JPanel makeLegend(Color color, String label) {
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        legend.setOpaque(false);
        JLabel colorBox = new JLabel("■");
        colorBox.setForeground(color);
        JLabel text = new JLabel(label);
        text.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        legend.add(colorBox);
        legend.add(text);
        return legend;
    }

    public static void main(String[] args) {
        new calendarr();
    }
}
