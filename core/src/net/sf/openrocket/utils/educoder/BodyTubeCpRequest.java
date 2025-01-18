package net.sf.openrocket.utils.educoder;

public class BodyTubeCpRequest {
    /**
     * 机翼中心
     */
    private Double planformCenter;
    /**
     * 机翼面积
     */
    private Double outerRadius;
    private Double planformArea;
    /**
     * 迎角的sinc值
     */
    private Double sincAOA;
    /**
     * 迎角的sin值
     */
    private Double sinAOA;
    /**
     * 计算中使用的参考面积
     */
    private Double refArea;
    /**
     * 当前马赫速度
     */
    private Double mach;
    /**
     * 迎角
     */
    private Double AOA;
    private Double length;
    private Integer divisions;
    /**
     * 正确结果
     */
    private Double answer;

    public Integer getDivisions() {
        return divisions;
    }

    public Double getOuterRadius() {
        return outerRadius;
    }

    public void setOuterRadius(Double outerRadius) {
        this.outerRadius = outerRadius;
    }

    public void setDivisions(Integer divisions) {
        this.divisions = divisions;
    }

    public void setDivisions(int divisions) {
        this.divisions = divisions;
    }

    public Double getLength() {
        return length;
    }

    public void setLength(Double length) {
        this.length = length;
    }

    public Double getPlanformCenter() {
        return planformCenter;
    }

    public void setPlanformCenter(Double planformCenter) {
        this.planformCenter = planformCenter;
    }

    public Double getPlanformArea() {
        return planformArea;
    }

    public void setPlanformArea(Double planformArea) {
        this.planformArea = planformArea;
    }

    public Double getSincAOA() {
        return sincAOA;
    }

    public void setSincAOA(Double sincAOA) {
        this.sincAOA = sincAOA;
    }

    public Double getSinAOA() {
        return sinAOA;
    }

    public void setSinAOA(Double sinAOA) {
        this.sinAOA = sinAOA;
    }

    public Double getRefArea() {
        return refArea;
    }

    public void setRefArea(Double refArea) {
        this.refArea = refArea;
    }

    public Double getMach() {
        return mach;
    }

    public void setMach(Double mach) {
        this.mach = mach;
    }

    public Double getAOA() {
        return AOA;
    }

    public void setAOA(Double AOA) {
        this.AOA = AOA;
    }

    public Double getAnswer() {
        return answer;
    }

    public void setAnswer(Double answer) {
        this.answer = answer;
    }

    @Override
    public String toString() {
        return "BodyTubeCpRequest{" +
                "planformCenter=" + planformCenter +
                ", planformArea=" + planformArea +
                ", sincAOA=" + sincAOA +
                ", sinAOA=" + sinAOA +
                ", refArea=" + refArea +
                ", mach=" + mach +
                ", AOA=" + AOA +
                ", answer=" + answer +
                '}';
    }
}