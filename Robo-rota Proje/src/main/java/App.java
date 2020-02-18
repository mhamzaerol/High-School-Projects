import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

import org.joda.time.DateTime;

import com.google.maps.DistanceMatrixApi;
import com.google.maps.DistanceMatrixApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DistanceMatrixElement;
import com.google.maps.model.DistanceMatrixRow;
import com.google.maps.model.TravelMode;
import com.teamdev.jxmaps.DirectionsRequest;
import com.teamdev.jxmaps.DirectionsResult;
import com.teamdev.jxmaps.DirectionsRouteCallback;
import com.teamdev.jxmaps.DirectionsStatus;
import com.teamdev.jxmaps.LatLng;
import com.teamdev.jxmaps.Map;
import com.teamdev.jxmaps.Marker;
import com.teamdev.jxmaps.swing.MapView;
public class App extends MapView{
	

    //final Browser browser = new Browser();
    //BrowserView browserView = new BrowserView(browser);
	public static Vector<Integer> Cev=new Vector<Integer>();
	public static MarkersExample sample = new MarkersExample();
	GeoApiContext context = new GeoApiContext().setApiKey("AIzaSyBMB3Os4pqQR8iybbU4Xjyxr7xnV7r_C5k");
	public static int N=0,start=-1;
	double topdk=0,topkm=0;
	String[] Data=new String[15];
	public static Vector<Double> Lats=new Vector<Double>();
	public static Vector<Double> Longs=new Vector<Double>();
	double[][] distMatrix=new double[15][15];
	double[][] dk=new double[15][15];
	double[][] km=new double[15][15];
	private JFrame fram;
	public static int sel;
	JPanel panel_10 = new JPanel();
	public static Marker Mrk;
	public static JTextField Long;
	public static JTextField Lat;
	DirectionsExample u;
	Map map;
	private JTextField Etiket;
	private JTable table;
	private JTable table_1;
	private final ButtonGroup buttonGroup_1 = new ButtonGroup();
	private final ButtonGroup buttonGroup_2 = new ButtonGroup();
	private final ButtonGroup buttonGroup = new ButtonGroup();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					App window = new App();
					window.fram.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	void PrintPath(Vector<Integer> V) {
		for(int i=0;i<V.size()-1;i++) {
			DefaultTableModel model=(DefaultTableModel) table_1.getModel();
			model.addRow(new Object[] {Integer.toString(i+1)+".",Data[V.elementAt(i)],Data[V.elementAt(i+1)],new DecimalFormat("##.##").format(km[V.elementAt(i)][V.elementAt(i+1)]),new DecimalFormat("##.##").format(dk[V.elementAt(i)][V.elementAt(i+1)])});
			topdk+=dk[V.elementAt(i)][V.elementAt(i+1)];
			topkm+=km[V.elementAt(i)][V.elementAt(i+1)];
		}
	}
	
	void Res() {
		N=0;
		start=-1;
		DefaultTableModel model=(DefaultTableModel) table.getModel();
		DefaultTableModel model1=(DefaultTableModel) table_1.getModel();
		for(int i=0;i<model.getRowCount();)
			model.removeRow(i);
		for(int i=0;i<model1.getRowCount();)
			model1.removeRow(i);
	}
	/**
	 * Create the application.
	 */
	public App() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		fram = new JFrame();
		fram.setFont(new Font("Dialog", Font.PLAIN, 25));
		fram.setTitle("Robo-Rota Tübitak Projesi");
		fram.setBounds(100, 100, 1220, 660);
		fram.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		fram.getContentPane().setLayout(null);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(0, 0, 1220, 660);
		fram.getContentPane().add(tabbedPane);
		
		JPanel panel_6 = new JPanel();
		panel_6.setBackground(new Color(255, 255, 255));
		tabbedPane.addTab("Giriş", null, panel_6, null);
		panel_6.setLayout(null);
		
		JLabel lblNewLabel_4 = new JLabel("");
		lblNewLabel_4.setBounds(45, 75, 200, 200);
		panel_6.add(lblNewLabel_4);
		Image img1=new ImageIcon(this.getClass().getResource("/tbtk.png")).getImage();
		lblNewLabel_4.setIcon(new ImageIcon(img1));
		
		JTextPane txtpnUygulamami = new JTextPane();
		txtpnUygulamami.setFont(new Font("Dialog", Font.PLAIN, 14));
		txtpnUygulamami.setBackground(UIManager.getColor("Button.shadow"));
		txtpnUygulamami.setEditable(false);
		txtpnUygulamami.setText("Robo-Rota\n\nFaklı konumlarda bulunan N adet noktayı bisikletle, araç ile ya da yayan olarak ziyaret ettikten sonra geri dönmeniz gerekirse en uygun rotanız nasıl olur? İşte bu soru bilinen adıyla Gezgin Satıcı Problemidir (TSP, Travelling Salesman Problem). Bu soru gazete dağıtıcısını, bir kuryeyi, bayramda akrabalarınız gezmek isteyen birini, müşterilerine mal veya hizmeti en kısa zamanda dağıtmak isteyen satıcıyı, turistik mekânları sırasıyla en az zamanda gezmek isteyen bir turist kafilesini yakından ilgilendiriyor.\n\nBu çalışmada TSP probleminin farklı algoritmik yöntemlerle çözülerek güncel hayata uyarlanması amaçlanmıştır. Projede, Google’ın Maps API (Application Programming Interface) hizmetlerinden yararlanarak herkes tarafından kullanılabilecek bir uygulama Java platformunda geliştirilmiştir. Uygulama, anlık olarak Google haritalarından trafik yoğunluk bilgisini alarak ziyaret edilmesi gereken N adet noktanın en kısa zamanda ya da en kısa km ile gezilmesini sağlamaktadır. Hazırlanan görsel bir GUI (Graphical User Interface) uygulaması ile de kullanıcı dostu bir çalışma gerçekleştirilmiştir. Ayrıca çalışmada seyahat yöntemi ve aracı olarak yaya, bisiklet ve araç (taksi, araba, otobüs vs.) seçenekleri vardır. A-Star gibi sezgisel arama yöntemleri ve Branch & Bound gibi kesmeli özyineleme algoritmaları ile farklı veri yapıları kullanılarak en optimal sonucun bulunması sağlanmıştır. Yapılan testlerde zamana ve mesafeye göre en uygun rota seçenekleri Google haritalarında gösterilmiştir.\n\nUygulamaya geçmek için üstteki uygulama sekmesine tıklayınız.");
		txtpnUygulamami.setBounds(300, 50, 700, 372);
		panel_6.add(txtpnUygulamami);
		
		JLabel lblRoborotaProjesi = new JLabel("Robo-Rota Projesi");
		lblRoborotaProjesi.setFont(new Font("Dialog", Font.BOLD, 18));
		lblRoborotaProjesi.setBounds(50, 300, 250, 25);
		panel_6.add(lblRoborotaProjesi);
		
		JPanel panel_4 = new JPanel();
		tabbedPane.addTab("Uygulama", null, panel_4, null);
		panel_4.setLayout(null);
		
		JPanel panel_8 = new JPanel();
		panel_8.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		panel_8.setBounds(10, 10, 690, 160);
		panel_4.add(panel_8);
		panel_8.setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setBounds(5, 5, 330, 150);
		panel_8.add(panel);
		panel.setLayout(null);
		
		JLabel lblAltitude = new JLabel("Latitude:");
		lblAltitude.setBounds(5, 25, 90, 20);
		panel.add(lblAltitude);
		lblAltitude.setFont(new Font("SansSerif", Font.BOLD, 12));
		
		JLabel lblLongtitude = new JLabel("Longtitude:");
		lblLongtitude.setBounds(5, 50, 90, 20);
		panel.add(lblLongtitude);
		
		Long = new JTextField();
		Long.setBounds(95, 50, 115, 20);
		panel.add(Long);
		Long.setColumns(10);
		
		Lat = new JTextField();
		Lat.setBounds(95, 25, 115, 20);
		panel.add(Lat);
		Lat.setColumns(10);
		
		JLabel lblEklemeBolumu = new JLabel("Konum Ekleme Bölümü:");
		lblEklemeBolumu.setFont(new Font("Dialog", Font.BOLD, 15));
		lblEklemeBolumu.setBounds(5, 0, 250, 20);
		panel.add(lblEklemeBolumu);
		
		JLabel lblEtiket = new JLabel("Etiket:");
		lblEtiket.setBounds(5, 75, 90, 20);
		panel.add(lblEtiket);
		
		Etiket = new JTextField();
		Etiket.setBounds(95, 75, 115, 20);
		panel.add(Etiket);
		Etiket.setColumns(10);
		
		JButton btnNewButton = new JButton("Ekle");
		btnNewButton.setBounds(215, 25, 100, 20);
		panel.add(btnNewButton);
		
		JButton btnNewButton_1 = new JButton("Güncelle");
		btnNewButton_1.setBounds(215, 50, 100, 20);
		panel.add(btnNewButton_1);
		
		JButton btnNewButton_2 = new JButton("Sil");
		btnNewButton_2.setBounds(215, 75, 100, 20);
		panel.add(btnNewButton_2);
		
		JButton btnSe = new JButton("Başlangıç Noktası Seç");
		btnSe.setBounds(130, 115, 190, 20);
		panel.add(btnSe);
		
		JButton btnNewButton_3 = new JButton("Tümünü Sil");
		btnNewButton_3.setBounds(5, 115, 115, 20);
		panel.add(btnNewButton_3);
		
		JPanel panel_2 = new JPanel();
		panel_2.setBounds(345, 5, 335, 150);
		panel_8.add(panel_2);
		panel_2.setLayout(null);
		
		JLabel lblNewLabel_2 = new JLabel("Eklenen Konumlar:");
		lblNewLabel_2.setFont(new Font("Dialog", Font.BOLD, 15));
		lblNewLabel_2.setBounds(5, 0, 325, 20);
		panel_2.add(lblNewLabel_2);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(5, 25, 325, 125);
		panel_2.add(scrollPane);
		
		table = new JTable();
		scrollPane.setViewportView(table);
		table.setModel(new DefaultTableModel(
			new Object[][] {
			},
			new String[] {
				"No", "Etiket", "Latitude", "Longtitude"
			}
		) {
			Class[] columnTypes = new Class[] {
				String.class, String.class, String.class, String.class
			};
			public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
			boolean[] columnEditables = new boolean[] {
				false, false, false, false
			};
			public boolean isCellEditable(int row, int column) {
				return columnEditables[column];
			}
		});
		table.getColumnModel().getColumn(0).setPreferredWidth(30);
		table.getColumnModel().getColumn(1).setPreferredWidth(150);
		table.getColumnModel().getColumn(2).setPreferredWidth(100);
		table.getColumnModel().getColumn(3).setPreferredWidth(100);
		table.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		
		
		Image img3=new ImageIcon(this.getClass().getResource("/front-car.png")).getImage();
		Image img5=new ImageIcon(this.getClass().getResource("/bicycle.png")).getImage();
		Image img6=new ImageIcon(this.getClass().getResource("/pedestrian-walking.png")).getImage();
		
		JPanel panel_9 = new JPanel();
		panel_9.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		panel_9.setBounds(10, 180, 330, 380);
		panel_4.add(panel_9);
		panel_9.setLayout(null);
		
		JPanel panel_3 = new JPanel();
		panel_3.setBounds(5, 265, 230, 80);
		panel_9.add(panel_3);
		panel_3.setLayout(null);
		
		final JRadioButton Msf = new JRadioButton("Mesafeye Göre");
		Msf.setBounds(5, 30, 132, 20);
		panel_3.add(Msf);
		buttonGroup_2.add(Msf);
		
		JRadioButton Time = new JRadioButton("Zamana Göre");
		Time.setBounds(5, 60, 127, 20);
		panel_3.add(Time);
		buttonGroup_2.add(Time);
		
		JLabel lblHesaplamaModunuSeiniz = new JLabel("Hesaplama Modunu Seçiniz:");
		lblHesaplamaModunuSeiniz.setBounds(2, 0, 228, 20);
		panel_3.add(lblHesaplamaModunuSeiniz);
		lblHesaplamaModunuSeiniz.setFont(new Font("Dialog", Font.BOLD, 15));
		
		JButton button = new JButton("Hesapla");
		button.setBounds(5, 355, 100, 20);
		panel_9.add(button);
		
		JPanel panel_7 = new JPanel();
		panel_7.setBounds(5, 5, 270, 75);
		panel_9.add(panel_7);
		panel_7.setLayout(null);
		
		final JRadioButton Car = new JRadioButton("Araba");
		Car.setFont(new Font("Dialog", Font.BOLD, 12));
		Car.setBounds(0, 50, 70, 20);
		panel_7.add(Car);
		buttonGroup_1.add(Car);
		
		final JRadioButton Bcy = new JRadioButton("Bisiklet");
		Bcy.setFont(new Font("Dialog", Font.BOLD, 12));
		Bcy.setBounds(90, 50, 80, 20);
		panel_7.add(Bcy);
		buttonGroup_1.add(Bcy);
		
		final JRadioButton Walk = new JRadioButton("Yürüme");
		Walk.setFont(new Font("Dialog", Font.BOLD, 12));
		Walk.setBounds(190, 50, 80, 20);
		panel_7.add(Walk);
		buttonGroup_1.add(Walk);
		
		JLabel lblNewLabel_3 = new JLabel("");
		lblNewLabel_3.setBounds(25, 25, 24, 24);
		panel_7.add(lblNewLabel_3);
		lblNewLabel_3.setIcon(new ImageIcon(img3));
		
		JLabel label = new JLabel("");
		label.setBounds(115, 25, 24, 24);
		panel_7.add(label);
		label.setIcon(new ImageIcon(img5));
		
		JLabel label_1 = new JLabel("");
		label_1.setBounds(215, 25, 24, 24);
		panel_7.add(label_1);
		label_1.setIcon(new ImageIcon(img6));
		
		JLabel lblSeyahatModu = new JLabel("Seyahat Modu Seçiniz:");
		lblSeyahatModu.setFont(new Font("Dialog", Font.BOLD, 15));
		lblSeyahatModu.setBounds(0, 0, 239, 15);
		panel_7.add(lblSeyahatModu);
		
		JPanel panel_5 = new JPanel();
		panel_5.setBounds(5, 85, 300, 170);
		panel_9.add(panel_5);
		panel_5.setLayout(null);
		
		final JRadioButton AB = new JRadioButton("Branch & Bound + A Star");
		buttonGroup.add(AB);
		AB.setBounds(5, 30, 300, 20);
		panel_5.add(AB);
		
		final JRadioButton A = new JRadioButton("A* (A Star)");
		buttonGroup.add(A);
		A.setBounds(5, 60, 200, 20);
		panel_5.add(A);
		
		final JRadioButton G = new JRadioButton("Greedy (Açgözlü)");
		buttonGroup.add(G);
		G.setBounds(5, 90, 200, 20);
		panel_5.add(G);
		
		final JRadioButton E = new JRadioButton("Exhaustive Search (Brute Force)");
		buttonGroup.add(E);
		E.setBounds(5, 120, 300, 20);
		panel_5.add(E);
		
		final JRadioButton BM = new JRadioButton("Bitmask (Dinamik Programlama)");
		buttonGroup.add(BM);
		BM.setBounds(5, 150, 266, 20);
		panel_5.add(BM);
		
		JLabel lblAlgoritmaSeiniz = new JLabel("Algoritma seçiniz:");
		lblAlgoritmaSeiniz.setFont(new Font("Dialog", Font.BOLD, 15));
		lblAlgoritmaSeiniz.setBounds(0, 0, 250, 20);
		panel_5.add(lblAlgoritmaSeiniz);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		panel_1.setBounds(360, 180, 340, 380);
		panel_4.add(panel_1);
		panel_1.setLayout(null);
		
		JLabel lblRota = new JLabel("Rota:");
		lblRota.setFont(new Font("Dialog", Font.BOLD, 15));
		lblRota.setBounds(5, 0, 250, 20);
		panel_1.add(lblRota);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(5, 25, 325, 200);
		panel_1.add(scrollPane_1);
		
		table_1 = new JTable();
		scrollPane_1.setViewportView(table_1);
		table_1.setModel(new DefaultTableModel(
			new Object[][] {
			},
			new String[] {
				"No", "Ba\u015Flang\u0131\u00E7", "Biti\u015F", "Km", "Dk"
			}
		) {
			Class[] columnTypes = new Class[] {
				Object.class, String.class, String.class, String.class, Object.class
			};
			public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
			boolean[] columnEditables = new boolean[] {
				false, false, false, false, false
			};
			public boolean isCellEditable(int row, int column) {
				return columnEditables[column];
			}
		});
		table_1.getColumnModel().getColumn(0).setPreferredWidth(30);
		table_1.getColumnModel().getColumn(1).setPreferredWidth(125);
		table_1.getColumnModel().getColumn(2).setPreferredWidth(125);
		table_1.getColumnModel().getColumn(3).setPreferredWidth(50);
		table_1.getColumnModel().getColumn(4).setPreferredWidth(50);
		table_1.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		
		final JTextPane GZR = new JTextPane();
		GZR.setBounds(10, 255, 300, 30);
		panel_1.add(GZR);
		GZR.setEditable(false);
		
		JLabel lblNewLabel_1 = new JLabel("Toplam Km:");
		lblNewLabel_1.setFont(new Font("Dialog", Font.BOLD, 15));
		lblNewLabel_1.setBounds(5, 230, 300, 20);
		panel_1.add(lblNewLabel_1);
		
		JLabel lblToplamDakika = new JLabel("Toplam Dakika:");
		lblToplamDakika.setFont(new Font("Dialog", Font.BOLD, 15));
		lblToplamDakika.setBounds(5, 295, 140, 20);
		panel_1.add(lblToplamDakika);
		
		final JTextPane textPane = new JTextPane();
		textPane.setEditable(false);
		textPane.setBounds(10, 320, 300, 30);
		panel_1.add(textPane);
		
		panel_10.setBounds(715, 10, 470, 550);
		panel_4.add(panel_10);
        panel_10.setLayout(null);
        sample.setSize(470, 550);
        panel_10.add(sample);
        
        Image img2=new ImageIcon(this.getClass().getResource("/map.png")).getImage();
		fram.setIconImage(img2);
		
	       
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				DefaultTableModel model=(DefaultTableModel) table.getModel();
				int selected=table.getSelectedRow();
				if(selected==-1) {
					JOptionPane.showMessageDialog(null,"Lütfen Güncellemek İçin Bir Satır Seçiniz!");
				}
				else {
					model.setValueAt(Etiket.getText(),selected,1);
					model.setValueAt(Lat.getText(),selected,2);
					model.setValueAt(Long.getText(),selected,3);
					Lat.setText("");
					Long.setText("");
					Etiket.setText("");
				}
			}
		});
		
		btnSe.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(table.getSelectedRow()==-1) {
					JOptionPane.showMessageDialog(null,"Lütfen Başlangıç Noktası İçin Bir Satır Seçiniz!!!");
				}
				else {
					start=table.getSelectedRow();
				}
			}
		});
		
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					String S1=Lat.getText();
					String S2=Long.getText();
					String S3=Etiket.getText();
					Double.parseDouble(S1);
					Double.parseDouble(S2);
					S3.charAt(0);
					N++;
					DefaultTableModel model=(DefaultTableModel) table.getModel();
					model.addRow(new Object[] {Integer.toString(N)+".",S3,S1,S2});
					Lat.setText("");
					Long.setText("");
					Etiket.setText("");
				}
				catch(java.lang.NumberFormatException e) {
					JOptionPane.showMessageDialog(null,"Lütfen Kutucuklara Geçerli Değerler Giriniz!");
				}
				catch(java.lang.StringIndexOutOfBoundsException e) {
					JOptionPane.showMessageDialog(null,"Etiket Kutucuğu Boş Bırakılamaz!");	
				}
			}
		});
		
		btnNewButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				DefaultTableModel model=(DefaultTableModel) table.getModel();
				int selected=table.getSelectedRow();
				if(selected==-1) {
					JOptionPane.showMessageDialog(null,"Lütfen Silmek İçin Satır Seçiniz!");
				}
				else {
					N--;
					model.removeRow(selected);
					for(int i=selected;i<N;i++) {
						model.setValueAt(Integer.toString(i+1),i,0);
					}
				}
			}
		});
		
		btnNewButton_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Res();
				Lats.clear();
				Longs.clear();
				GZR.setText("");
				textPane.setText("");
				sample.marker.setVisible(false);
				if(u!=null) {
					panel_10.remove(u);
					panel_10.add(sample);		
				}
			}
		});
		
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
	        	topdk=0;topkm=0;
				if(start!=-1) {
					Mrk.setVisible(false);
					String[] origins=new String[N];
					String[] destinations=new String[N];
					DefaultTableModel model=(DefaultTableModel) table_1.getModel();
					for(int i=0;i<model.getRowCount();)
						model.removeRow(i);
					for(int i=0;i<N;i++) {
						String S=table.getValueAt(i,1).toString();
						String x=table.getValueAt(i,2).toString();
						String y=table.getValueAt(i,3).toString();
						Data[i]=S;
						origins[i]=x+","+y;
						destinations[i]=x+","+y;
						Lats.addElement(Double.parseDouble(x));
						Longs.addElement(Double.parseDouble(y));
					}
					TravelMode q = null;
					if(Car.isSelected())
						q=TravelMode.DRIVING;
					if(Bcy.isSelected())
						q=TravelMode.BICYCLING;	
					if(Walk.isSelected())
						q=TravelMode.WALKING;		
					DistanceMatrixApiRequest x=DistanceMatrixApi.getDistanceMatrix(context,origins,destinations).mode(q).departureTime(DateTime.now());
					try {
						DistanceMatrixRow[] res=x.await().rows;
						for(int i=0;i<N;i++) {
							DistanceMatrixElement[] el=res[i].elements;
							for(int j=0;j<N;j++) {
								km[i][j]=el[j].distance.inMeters/1000.0;
								if(q==TravelMode.DRIVING)
									dk[i][j]=el[j].durationInTraffic.inSeconds/60.0;
								else
									dk[i][j]=el[j].duration.inSeconds/60.0;
								if(Msf.isSelected()) {
									distMatrix[i][j]=el[j].distance.inMeters/1000.0;
								}
								else {
									if(q==TravelMode.DRIVING) {
										distMatrix[i][j]=el[j].durationInTraffic.inSeconds/60.0;
									}
									else {
										distMatrix[i][j]=el[j].duration.inSeconds/60.0;	
									}
								}
							}
						}
					}
					catch (NullPointerException e) {
						JOptionPane.showMessageDialog(null,"Bisiklet Kullanılamıyor!");
					} 
					catch (Exception e) {
						e.printStackTrace();
					}
					if(AB.isSelected()) {
						BranchAndBound Ans=new BranchAndBound(distMatrix,start,N);
						Cev=Ans.Answer;
						PrintPath(Ans.Answer);
						GZR.setText(new DecimalFormat("##.##").format(topkm));
						textPane.setText(new DecimalFormat("##.##").format(topdk));
					}
					else if(A.isSelected()) {
						AStar Ans=new AStar(distMatrix,start,N);
						Cev=Ans.Answer;
						PrintPath(Ans.Answer);	
						GZR.setText(new DecimalFormat("##.##").format(topkm));
						textPane.setText(new DecimalFormat("##.##").format(topdk));
					}
					else if(G.isSelected()) {
						Greedy Ans=new Greedy(distMatrix,start,N);
						Cev=Ans.Answer;
						PrintPath(Ans.Answer);	
						GZR.setText(new DecimalFormat("##.##").format(topkm));
						textPane.setText(new DecimalFormat("##.##").format(topdk));
					}
					else if(E.isSelected()) {
						Exh Ans=new Exh(distMatrix,start,N);
						Cev=Ans.Answer;
						PrintPath(Ans.Answer);
						GZR.setText(new DecimalFormat("##.##").format(topkm));
						textPane.setText(new DecimalFormat("##.##").format(topdk));
					}
					else if(BM.isSelected()) {
						BitM Ans=new BitM(distMatrix,start,N);
						Cev=Ans.Answer; 
						PrintPath(Ans.Answer);	
						GZR.setText(new DecimalFormat("##.##").format(topkm));
						textPane.setText(new DecimalFormat("##.##").format(topdk));	
					}
					else {
						JOptionPane.showMessageDialog(null,"Lütfen Bir Algoritma Türü Seçin!!!");
					}
				}
				else {
					JOptionPane.showMessageDialog(null,"Lütfen Bir Başlangıç Noktası Seçin!!!");
				}
			}
		});
		
		table_1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(u!=null) {
					panel_10.remove(u);
				}
				else {
					panel_10.remove(sample);
				}
				//new LatLng(App.Lats.elementAt(App.Cev.elementAt(App.sel)),App.Longs.elementAt(App.Cev.elementAt(App.sel)));
				sel=table_1.getSelectedRow(); 
				u=new DirectionsExample();
				u.setSize(470,550);
				panel_10.add(u);
				u.setEnabled(true);
			}
		});
	}
}
