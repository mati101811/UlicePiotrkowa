package mat.szu.ulicepiotrkowa;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

public class Street
{
    private String fullName;
    private String simpleName;
    private ArrayList<ArrayList<GeoPoint>> points;
    
    public void addPointsList(ArrayList<GeoPoint> points)
    {
        this.points.add(points);
    }
    
    public String getFullName( )
    {
        return fullName;
    }
    
    public void setFullName(String fullName)
    {
        this.fullName = fullName;
    }
    
    public String getSimpleName( )
    {
        return simpleName;
    }
    
    public void setSimpleName(String simpleName)
    {
        this.simpleName = simpleName;
    }
    
    public ArrayList<ArrayList<GeoPoint>> getPoints( )
    {
        return points;
    }
    
    public void setPoints(ArrayList<ArrayList<GeoPoint>> points)
    {
        this.points = points;
    }
    
}
