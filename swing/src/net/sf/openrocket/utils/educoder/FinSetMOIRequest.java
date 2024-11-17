package net.sf.openrocket.utils.educoder;

public class FinSetMOIRequest{
    private Double length;
    /**
     * 翼展
     */
    private Double span;

    private Double finArea;

    private Integer finCount;

    private Double bodyRadius;
    private Double singlePlanformArea;
    private Double[] answer;

    public Double getSinglePlanformArea() {
        return singlePlanformArea;
    }

    public void setSinglePlanformArea(Double singlePlanformArea) {
        this.singlePlanformArea = singlePlanformArea;
    }

    public Double[] getAnswer() {
        return answer;
    }

    public void setAnswer(Double[] answer) {
        this.answer = answer;
    }

    public Double getBodyRadius() {
        return bodyRadius;
    }

    public void setBodyRadius(Double bodyRadius) {
        this.bodyRadius = bodyRadius;
    }

    public Double getLength() {
        return length;
    }

    public void setLength(Double length) {
        this.length = length;
    }

    public Double getSpan() {
        return span;
    }

    public void setSpan(Double span) {
        this.span = span;
    }

    public Double getFinArea() {
        return finArea;
    }

    public void setFinArea(Double finArea) {
        this.finArea = finArea;
    }

    public Integer getFinCount() {
        return finCount;
    }

    public void setFinCount(Integer finCount) {
        this.finCount = finCount;
    }

    @Override
    public String toString() {
        return "FinSetMOIRequest{" +
                "length=" + length +
                ", span=" + span +
                ", finArea=" + finArea +
                ", finCount=" + finCount +
                ", bodyRadius=" + bodyRadius +
                ", answer=" + answer +
                '}';
    }
}
