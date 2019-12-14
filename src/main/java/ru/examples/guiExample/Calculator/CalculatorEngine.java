package ru.examples.guiExample.Calculator;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JTextField;

public class CalculatorEngine implements ActionListener {
	
	Calculator parent; // ссылка на Calculator
	
	char selectedAction = ' '; // +, -, /, или *
	double currentResult =0;
	
	// Конструктор сохраняет ссылку на окно калькулятора в переменной класса “formProgressBar”
	CalculatorEngine(Calculator parent){
		this.parent = parent;
	}
	
	public void actionPerformed(ActionEvent e){
		double displayValue=0;
		
		JTextField myDisplayField=null;
		JButton clickedButton=null;
		
		Object eventSource = e.getSource();
		
		if (eventSource instanceof JButton){
			clickedButton = (JButton) eventSource; // Получаем источник текущего действия
		}else if (eventSource instanceof JTextField){
			myDisplayField = (JTextField)eventSource;
		}
		
		// Получить текущий текст из поля вывода (“дисплея”) калькулятора
		String dispFieldText = parent.displayField.getText();
		
		if (!"".equals(dispFieldText)){
			displayValue= Double.parseDouble(dispFieldText);
		}		
		
		if (eventSource == parent.buttonClear){
			displayValue = 0;
			currentResult = 0;
			parent.displayField.setText("");
		} else if (eventSource == parent.buttonClearLast){
			if (dispFieldText.length() > 0) {
				dispFieldText = dispFieldText.substring(0, dispFieldText.length()-1);
				parent.displayField.setText(dispFieldText);
				if (!"".equals(dispFieldText)){
					displayValue= Double.parseDouble(dispFieldText);
				}	
			}
			
		} else if (eventSource == parent.buttonPlus){
			selectedAction = '+';
			currentResult=displayValue;
			parent.displayField.setText("");
		} else if (eventSource == parent.buttonMinus){
			selectedAction = '-';
			currentResult=displayValue;
			parent.displayField.setText("");
		}else if (eventSource == parent.buttonDivide){
			selectedAction = '/';
			currentResult=displayValue;
			parent.displayField.setText("");
		} else if (eventSource == parent.buttonMultiply){
			selectedAction = '*';
			currentResult=displayValue;
			parent.displayField.setText("");
		} else if (eventSource == parent.buttonEqual){
			// Совершить арифметическое действие, в зависимости
			// от selectedAction, обновить переменную currentResult
			// и показать результат на дисплее
			if (selectedAction=='+'){
				currentResult+=displayValue;
				// Сконвертировать результат в строку, добавляя его
				// к пустой строке и показать его
				parent.displayField.setText(""+currentResult);
			}else if (selectedAction=='-'){
				currentResult -=displayValue;
				parent.displayField.setText(""+currentResult);
			}else if (selectedAction=='/'){
				currentResult /=displayValue;
				parent.displayField.setText(""+currentResult);
			}else if (selectedAction=='*'){
				currentResult*=displayValue;
				parent.displayField.setText(""+currentResult);
			}
		} else{
			// Для всех цифровых кнопок присоединить надпись на кнопке к надписи в дисплее
			String clickedButtonLabel= clickedButton.getText();
			if ((clickedButtonLabel != ".") || (dispFieldText.indexOf('.') == -1)) {
				parent.displayField.setText(dispFieldText +
				clickedButtonLabel);
			}
		}
			
		
/*
		// Получаем источник текущего действия
		JButton clickedButton= (JButton)e.getSource();
*/		
		// Получаем надпись на кнопке
		String clickedButtonLabel = clickedButton.getText();	
		

//		JOptionPane.showConfirmDialog(null,	"Теущее значение " + dispFieldText, "Сообщение",	JOptionPane.PLAIN_MESSAGE);

		
		// Новое значение
//		formProgressBar.displayField.setText(dispFieldText +	clickedButtonLabel);
		
/*		
		JOptionPane.showConfirmDialog(null,	"Нажата кнопка " + clickedButtonLabel, 
				"Just a test",
				JOptionPane.PLAIN_MESSAGE);

		// Для получения текущего системного времени достаточно выполнить: 
		long curTime = System.currentTimeMillis(); 

		// Хотите значение типа Date, с этим временем? 
		Date curDate = new Date(curTime); 
		
		// Хотите строку в формате, удобном Вам? 
		String curStringDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(curTime); 

		clickedButton.setText(curStringDate);
*/		
		
	}
}