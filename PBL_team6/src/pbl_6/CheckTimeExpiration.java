package pbl_6;
import java.util.*;
import java.time.*;
import java.awt.event.*;

public class CheckTimeExpiration implements ActionListener{
	
	private FoodManage.MainFrame foodManage;
	

	//메인의 List를 얻기위해 객체의 연결을 진행
	public CheckTimeExpiration(FoodManage.MainFrame _foodManage) {
		this.foodManage = _foodManage;
	}
	
	public void actionPerformed(ActionEvent e) {
		ArrayList<Integer> alarmIndex = new ArrayList<>(); //알람 기능에 전달 배열
		ArrayList<Food> foodList = foodManage.getFoodList();
		
		LocalDate today = LocalDate.now();
		LocalDate targetDate = today.plusDays(3);//현재 3일로 설정
		
		for (int i = 0;i<foodList.size();i++) { //foodList 내부 객체 확인 반복문
			Food food = foodList.get(i);
			String foodName = food.name;
			LocalDate expirationDate = food.expirationDate;
			
			//day의 차이가 0밑으로 만들면> 날짜 안지남 + 곧 설정할 알림 날짜(현재 임시)보다 작은 경우 실행
			if((today.compareTo(expirationDate) <= 0) && (expirationDate.compareTo(targetDate) <= 0)){
				alarmIndex.add(i);//알람에 전달할 현재 탐색 인덱스 추가
			}
			
		}
		if(!alarmIndex.isEmpty()) { //인덱스 비어있지 않는 경우 실행
			//AlarmExpirationDate alarm = new alarmExpirationDate(foodManage, alarmIndex);
		}
	}
}
