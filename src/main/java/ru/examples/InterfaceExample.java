package ru.examples;

public class InterfaceExample{

    public static void main(String[] args) {

        Circle circle = new Circle(10);
        circle.draw();
        System.out.println("Площадь круга с радиусом  " +
                circle.getRadius() +
                " равна " +
                circle.getArea());

        Rectangle rectangle = new Rectangle(2, 3);
        rectangle.draw();
        System.out.println("Площадь прямоугольника со сторонами "+
                rectangle.getWidth()+
                " x "+
                rectangle.getHeight()+
                " равна "+
                rectangle.getArea());
    }
}

class Circle implements Shape{

    private double radius;

    public Circle(double radius){
        this.radius = radius;
    }

    public double getRadius(){
        return this.radius;
    }

    @Override
    public void draw(){
        System.out.println("Рисуем круг");
    }

    @Override
    public double getArea(){
        return Math.PI * this.radius * this.radius;
    }

}

class Rectangle implements Shape{

    private double width;
    private double height;

    public Rectangle(double width, double height) {
        this.width = width;
        this.height = height;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    @Override
    public void draw(){
        System.out.println("Рисуем прямоугольник");
    }

    @Override
    public double getArea(){
        return width * height;
    }
}

/**
 * Интерфейс
 */
interface Shape {
    public String name = "Shape";

    void draw();

    double getArea();
}
