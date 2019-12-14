package ru.examples.guiExample.Calculator;

import javax.swing.*;
import javax.swing.text.NumberFormatter;

import java.awt.Color;
import java.awt.GridLayout;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.awt.BorderLayout;
//import java.awt.GridBagLayout;
//import java.awt.GridBagConstraints;



public class Calculator {
	// Объявление всех компонентов калькулятора.
	JPanel windowContent;
//	JTextField displayField;
	JFormattedTextField displayField;
	JButton button0;
	JButton button1;
	JButton button2;
	JButton button3;
	JButton button4;
	JButton button5;
	JButton button6;
	JButton button7;
	JButton button8;
	JButton button9;

	JButton buttonMinus;
	JButton buttonPlus;
	JButton buttonDivide;
	JButton buttonMultiply;
	JButton buttonPoint;
	JButton buttonEqual;
	JButton buttonClear;
	JButton buttonClearLast;

	JPanel p1;
	JPanel p2;
	
	// В конструкторе создаются все компоненты
	// и добавляются на фрейм с помощью комбинации
	// Borderlayout и Gridlayout
	Calculator(){
		windowContent= new JPanel();
		
		// создаем схему для этой панели
		BorderLayout bl = new BorderLayout();
		windowContent.setLayout(bl);
		
		// создаем и отображаем поле
//		displayField = new JTextField(30);
		
        NumberFormat number = new DecimalFormat("#,###.##");    
        number.setMaximumIntegerDigits(10);                 // максимальное количество циферок
        
		displayField = new JFormattedTextField(new NumberFormatter(number));
		displayField.setEnabled(false);
		displayField.setDisabledTextColor(Color.black);
		displayField.setColumns(15);
		displayField.setHorizontalAlignment(SwingConstants.RIGHT);
//		displayField.setValue(new Double(0.00));
//		displayField.setValue(new Double(1234567890.45));
		
		// отображаем в северной части
		windowContent.add("North",displayField);
		
		// создаем кнопки, используя конструктор класса JButton, который принимает текст кнопки в качестве параметра
		button0 = new JButton("0");
		button1 = new JButton("1");
		button2 = new JButton("2");
		button3 = new JButton("3");
		button4 = new JButton("4");
		button5 = new JButton("5");
		button6 = new JButton("6");
		button7 = new JButton("7");
		button8 = new JButton("8");
		button9 = new JButton("9");
		
		buttonClearLast = new JButton("<<");	
		buttonClear = new JButton("C");	
		buttonMinus = new JButton("-");
		buttonPlus = new JButton("+");
		buttonDivide = new JButton("/");
		buttonMultiply = new JButton("*");		
		buttonPoint = new JButton(".");
		buttonEqual = new JButton("=");		
		
		//слушатель нажатий
		CalculatorEngine calcEngine = new CalculatorEngine(this);
		
		button0.addActionListener(calcEngine);
		button1.addActionListener(calcEngine);
		button2.addActionListener(calcEngine);
		button3.addActionListener(calcEngine);
		button4.addActionListener(calcEngine);
		button5.addActionListener(calcEngine);
		button6.addActionListener(calcEngine);
		button7.addActionListener(calcEngine);
		button8.addActionListener(calcEngine);
		button9.addActionListener(calcEngine);

		buttonClear.addActionListener(calcEngine);
		buttonClearLast.addActionListener(calcEngine);
		buttonMinus.addActionListener(calcEngine);
		buttonPlus.addActionListener(calcEngine);
		buttonDivide.addActionListener(calcEngine);
		buttonMultiply.addActionListener(calcEngine);	
		buttonPoint.addActionListener(calcEngine);
		buttonEqual.addActionListener(calcEngine);		
		
		
		// создаем панель с GridLayout	
		// которая содержит 10 кнопок с числами
		// и кнопки с точкой и знаком равно
		p1 = new JPanel();
		GridLayout gl =new GridLayout(4,3,2,2);
		p1.setLayout(gl);
		
/*		
		
		// создем GridBagLayout для панели окна
		GridBagLayout gb = new GridBagLayout();
		p1.setLayout(gb);
		// создаем экземпляр класса GridBagConstraints
		// эти строки кода нужно повторить для каждой компоненты
		// которая добавляется в ячейку
		GridBagConstraints constr = new GridBagConstraints();
		//задаем ограничения для строки ввода калькулятора
		// координата x в таблице
//		constr.x=0;
		constr.gridx = 0;
		// координата y в таблице
//		constr.y=0;
		constr.gridy=0;
		// эта ячейка имеет такую же высоту, как стандартные ячейки
		constr.gridheight =1;
		// эта ячейка имеет ширину равную ширине 6 стандартных ячеек
		constr.gridwidth= 6;
		// заполняем все пространство ячейки
		constr.fill= constr.BOTH;
		// пропорция по горизонтали, которую будет заниматькомпонент
		constr.weightx = 1.0;
		// пропорция по вертикали, которую будет занимать компонент
		constr.weighty = 1.0;
		// позиция компонента внутри ячейки
		constr.anchor=constr.CENTER;
		displayField = new JTextField();
		// устанавливаем ограничения для поля ввода
		gb.setConstraints(displayField,constr);
		// добавляем поле ввода в окно
		windowContent.add(displayField);
		
		*/		
				
		// добавляем кнопки на панель p1
		p1.add(button7);
		p1.add(button8);
		p1.add(button9);
		
		p1.add(button4);
		p1.add(button5);
		p1.add(button6);
		
		p1.add(button1);
		p1.add(button2);
		p1.add(button3);
		
		p1.add(button0);
		p1.add(buttonPoint);

		// помещаем панель p1 в центральную область окна
		windowContent.add("Center",p1);

		//создаем панель дл¤ кнопок с действиями
		p2 = new JPanel();
		GridLayout gl2 =new GridLayout(4,2,2,2);
		p2.setLayout(gl2);	
		
		p2.add(buttonClear);
		p2.add(buttonClearLast);
		p2.add(buttonPlus);
		p2.add(buttonMinus);
		p2.add(buttonMultiply);
		p2.add(buttonDivide);
		p2.add(buttonEqual);


		// помещаем панель p2 в восточную область окна
		windowContent.add("East",p2);	
		
		
		//создаем фрейм и задаем его основную панель
		JFrame frame = new JFrame("Calculator");
		frame.setContentPane(windowContent);
		
		// делаем размер окна достаточным
		// для того, чтобы вместить все компоненты
		frame.pack();
		// наконец, отображаем окно
		frame.setVisible(true);
	}
	
	public static void main(String[] args) {
		Calculator calc = new Calculator();
	}
}