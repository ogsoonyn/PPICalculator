package com.gmail.ynakamura027.ppicalc;

import java.io.Serializable;
import java.math.BigDecimal;

public class DisplayProperty implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5497252264893943648L;
	private int m_width;
	private int m_height;
	private double m_inch;
	private double m_arw, m_arh;
	private int m_refresh;
	private String m_name;
    private int m_dsi_lane_num;
    private double m_compress_rate;
    private int m_color_depth;
    private double m_blanking_rate;


    public double get_compress_rate() {
        return m_compress_rate;
    }

    public int get_dsi_lane_num() {
        return m_dsi_lane_num;
    }

    public double get_blanking_rate() {
        return m_blanking_rate;
    }

    public int get_refresh() {
        return m_refresh;
    }

    public int get_color_depth() {
        return m_color_depth;
    }

    public DisplayProperty set_blanking_rate(double m_blanking_rate) {
        this.m_blanking_rate = m_blanking_rate;
        return this;
    }

    public DisplayProperty set_color_depth(int m_color_depth) {
        this.m_color_depth = m_color_depth;
        return this;
    }

    public DisplayProperty set_compress_rate(double m_compress_rate) {
        this.m_compress_rate = m_compress_rate;
        return this;
    }

    public DisplayProperty set_dsi_lane_num(int m_dsi_lane_num) {
        this.m_dsi_lane_num = m_dsi_lane_num;
        return this;
    }

    public DisplayProperty set_refresh(int m_refresh) {
        this.m_refresh = m_refresh;
        return this;
    }

	public DisplayProperty(){
		m_width = 640;
		m_height = 480;
		m_inch = 20;
		m_arw = 4;
		m_arh = 3;
		m_refresh = 60;

        m_dsi_lane_num = 4;
        m_compress_rate = 1;
        m_color_depth = 24;
        m_blanking_rate = 1.2;
	}
	
	public DisplayProperty(int w, int h, double inch)
	{
        this();
		Initialize(w, h, inch, "-");
	}
	
	public DisplayProperty(int w, int h, double inch, String name)
	{
        this();
		Initialize(w, h, inch, name);
	}

    public DisplayProperty(int w, int h, double inch, String name,
                           int dsi_lane, double compress, int depth, int refresh)
    {
        this();
        m_dsi_lane_num = dsi_lane;
        m_compress_rate = compress;
        m_color_depth = depth;
        m_refresh = refresh;
        Initialize(w, h, inch, name);
    }

    public void Update(int w, int h, double inch, String name){
        Initialize(w, h, inch, name);
        // TODO:
    }
	
	private void Initialize(int w, int h, double inch, String name){
		m_width = w;
		m_height = h;
		m_inch = inch;

		m_name = name;

        // 最小値設定
    	if( m_width<50){
    		m_width=50;
    	}
    	if( m_height<50){
    		m_height=50;
    	}
    	if( m_inch<0.1){
    		m_inch=0.1;
    	}

    	// アスペクト比を計算
        int tmp;
        int a,b;
        if(m_width > m_height){
            a = m_width;
            b = m_height;
        }else{
            a = m_height;
            b = m_width;
        }
        while((tmp = a % b) != 0){
            a = b;
            b = tmp;
        }

        m_arh = m_height/b;
        m_arw = m_width/b;
	}
	
	public int getWidth(){
		return m_width;
	}
	public int getHeight(){
		return m_height;
	}
	public double getInch(){
		return m_inch;
	}
	
	public String getName(){
		return m_name;
	}
	
	public double getARW(){
		return m_arw;
	}
	public double getARH(){
		return m_arh;
	}
	
	public Double calcPpi(){
		return Math.sqrt(m_width*m_width + m_height*m_height) / m_inch;
	}
	
	public Integer calcPixel(){
		return (m_width * m_height);
	}
	
	public Double calcPixelClock(){
		return m_refresh * m_width * m_height * m_blanking_rate;
	}

    public Double calcBitClock(){
        return calcPixelClock() * m_color_depth * m_compress_rate;
    }

    public Double calcByteClock(){
        return calcBitClock() / 8;
    }

    public Double calcDsiClock(){
        return calcBitClock()/m_dsi_lane_num;
    }

	public Double calcAspectRatio(){
		if( m_width > m_height ){
			return (double)m_height / m_width * 16; 
		}else{
			return (double)m_width / m_height * 16; 
		}
	}

	public Double calcDisplayInchW(){
		double ar = calcAspectRatio();
		return Math.sqrt(m_inch * m_inch / (256.0 + ar * ar)) * ar;
	}

	public Double calcDisplayInchH(){
		double ar = calcAspectRatio();
		return Math.sqrt(m_inch * m_inch / (256.0 + ar * ar)) * 16.0;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DisplayProperty other = (DisplayProperty)obj;
		return ( other.getWidth() == m_width &&
				 other.getHeight() == m_height &&
				 other.getInch() == m_inch);

    }
	
	@Override
	public String toString(){
		return "[" + m_name + "] "
                + Integer.toString(m_width) + " x " + Integer.toString(m_height)
                + "@" + Integer.toString(m_refresh) + "Hz"
				+ " / " + Double.toString(m_inch) + "\" ("
                + Math.round(calcPpi()) + "ppi)";
	}
}
