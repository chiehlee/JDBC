import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Solution {

	private Connection dbconnection;

	public class Product {
		private int id;
		private double weight;
		private double volume;

		Product(int id, double weight, double volume) {
			this.id = id;
			this.weight = weight;
			this.volume = volume;
		}
		
		public int getID() {
			return this.id;
		}
		
		public double getWeight() {
			return this.weight;
		}
		
		public double getVolume() {
			return this.volume;
		}

	}

	/*
	 * public class Invoice { private int id; private Date shippedOn;
	 * ArrayList<Product> products; int groupingId;
	 * 
	 * Invoice(int id, Date shippedOn,int groupingId, ArrayList<Product>
	 * products) { this.id = id; this.shippedOn = shippedOn; this.groupingId =
	 * groupingId; this.products = products; }
	 * 
	 * }
	 */

	public class Box {
		private int id;
		double maximumWeight;
		double volume;
		double currentWeight;
		double currentVolume;
		int fulfill;
		ArrayList<Product> contained;

		Box(int id, double maximumWeight, double volume, int fulfill) throws SQLException {
			this.id = id;
			this.maximumWeight = this.getMaximumWeight();
			this.volume = this.getMaximumVolume();
			this.fulfill = fulfill;
			
		}

		public int getID() {
			return this.id;
		}
		
		private double getMaximumWeight() throws SQLException {
			PreparedStatement ps = dbconnection
			        .prepareStatement("select b.maximumWeight from Box b where b.id=?");
			ps.setInt(1, this.id);
			ResultSet rs = ps.executeQuery();
			return rs.getDouble(1);
		}
		
		private double getMaximumVolume() throws SQLException {
			PreparedStatement ps = dbconnection
			        .prepareStatement("select b.volume from Box b where b.id=?");
			ps.setInt(1, this.id);
			ResultSet rs = ps.executeQuery();
			return rs.getDouble(1);
		}
		
		private double getCurrentWeight() {
			double d = 0;
			for (int i = 0; i < contained.size(); i++) {
				d = d + contained.get(i).getWeight();
			}
			return d;
		}
		
		private double getCurrentVolume() {
			double d = 0;
			for (int i = 0; i < contained.size(); i++) {
				d = d + contained.get(i).getVolume();
			}
			return d;  
		}

		public boolean isOversize() {
			return false;
		}

		public boolean isOverWeight() {
			return false;
		}

		// given a list of product add them into a box
		public void addListProductIntoBox(ArrayList<Product> LP)
		        throws SQLException {
			for (int i = 0; i < LP.size(); i++) {
				if ((LP.get(i).getWeight() + this.getCurrentWeight()) > this
				        .getMaximumWeight()) {
					throw new SQLException("over weight!");
				}
				if ((LP.get(i).getWeight() + this.getCurrentVolume()) > this
				        .getMaximumVolume()) {
					throw new SQLException("over weight!");
				} else {
					this.contained.add(LP.get(i));
				}
			}

		}
	}

	public Solution(String driver, String url, String user,
	        String password) {
		try {
			Class.forName(driver);
			dbconnection = DriverManager.getConnection(url, user,
			        password);
		} catch (Exception e) {
			System.err
			        .println("Unable to connect to the database due to "
			                + e);
		}
	}

	// get all the grouping Id base on today's invoices
	public ArrayList<Integer> getTodayInvoiceID() throws SQLException {

		// get today's date
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Date d = new Date();
		String today = df.format(d);
		//

		PreparedStatement invoices = dbconnection
		        .prepareStatement("select i.id from Invoice i where i.shippedOn=?");
		invoices.setString(1, today);
		ResultSet invoicesToday = invoices.executeQuery();
		ArrayList<Integer> invoicesIDs = new ArrayList<Integer>();
		invoicesToday.first();
		while (!invoicesToday.isLast()) {
			invoicesIDs.add(invoicesToday.getInt(1));
			invoicesToday.next();
		}
		return invoicesIDs;
	}

	
	
	public void preSetBox(ArrayList<Box> LBox) throws SQLException {
		for (int i = 0; i < LBox.size(); i++) {
			PreparedStatement preContainment = dbconnection
			        .prepareStatement("select c.contains from Containment "
			                + "c where c.isContainedIn=?");
			preContainment.setInt(1, LBox.get(i).getID());
			ResultSet containment = preContainment.executeQuery();
			containment.first();
			Product p;
			ArrayList<Product> LP = new ArrayList<Product>();
			PreparedStatement preProduct;
			ResultSet rs;
			while (!containment.isLast()) {
				preProduct = dbconnection
				        .prepareStatement("select p.id, p.weight, p.volume "
				                + "from Product p where p.id=?");
				preProduct.setInt(1, containment.getInt(1));
				rs = preProduct.executeQuery();
				p = new Product(rs.getInt(1),
				        rs.getDouble(2),
				        rs.getDouble(3));
				LP.add(p);
				containment.next();
			}
			LBox.get(i).addListProductIntoBox(LP);
		}
	}

	public ArrayList<Box> getBoxWithProductIn(int invoiceID)
	        throws SQLException {
		PreparedStatement preboxWithInvoice = dbconnection
		        .prepareStatement("select b.id from Box b where b.fulfills=?");
		preboxWithInvoice.setInt(1, invoiceID);
		ResultSet boxWithInvoice = preboxWithInvoice.executeQuery();
		ArrayList<Integer> ListBoxWithInvoice = new ArrayList<Integer>();
		ArrayList<Box> ListBox = new ArrayList<Box>();
		boxWithInvoice.first();
		while (!boxWithInvoice.last()) {
			ListBoxWithInvoice.add(boxWithInvoice.getInt(1));
			boxWithInvoice.next();
		}

		for (int i = 0; i < ListBoxWithInvoice.size(); i++) {
			PreparedStatement preBox = dbconnection
			        .prepareStatement("select b.id, b.maximumWeight, b.volFume, "
			                + "b.fulfills from Box b where b.id=?");
			preBox.setInt(1, ListBoxWithInvoice.get(i));
			ResultSet box = preBox.executeQuery();
			ListBox.add(new Box(box.getInt(1),
			        box.getDouble(2),
			        box.getDouble(3),
			        box.getInt(4)));
		}
		preSetBox(ListBox);
		return ListBox;

	}

	// gain information of product from containment base on given invoice ID and
	// make it into
	// a ArrayList<Product> which product class has fields of a product's id,
	// weight and volume.
	public ArrayList<Product> getUnpackedProductsFromInvoice(
	        int invoiceID) throws SQLException {
		PreparedStatement preContainment = dbconnection
		        .prepareStatement("select c.contains from Containment c where c.isContainedIn=?");
		preContainment.setInt(2, invoiceID);
		ResultSet containment = preContainment.executeQuery();
		ArrayList<Product> ListProduct = new ArrayList<Product>();
		while (!containment.last()) {
			PreparedStatement preProduct = dbconnection
			        .prepareStatement("select p.id, p.weight, p.volume from Product p where p.id=?");
			preProduct.setInt(1, containment.getInt(1));
			ResultSet product = preProduct.executeQuery();
			product.first();
			ListProduct.add(new Product(product.getInt(1), product
			        .getDouble(2), product.getDouble(3)));
		}
		return ListProduct;
	}
	
	public void addListProductintoListBox(int invoiceID)
	        throws SQLException {
		ArrayList<Product> unpackedProduct = getUnpackedProductsFromInvoice(invoiceID);
		ArrayList<Box> boxes = getBoxWithProductIn(invoiceID);
		for (int i = 0; i < boxes.size(); i++) {
			Box b = boxes.get(i);
			b.addListProductIntoBox(unpackedProduct);
			for (int j = 0; j < b.contained.size(); j++) {
				PreparedStatement ps = dbconnection
				        .prepareStatement("UPDATE Containment SET primary key=?,? WHERE contains=?");
				ps.setInt(1, b.contained.get(j).getID());
				ps.setInt(2, boxes.get(i).getID());
				ps.setInt(3, b.contained.get(j).getID());
				ps.execute();
			}
		}

	}


	public static void main(String[] args) throws SQLException {
		Solution s = new Solution("com.mysql.jdbc.Driver",
		        "jdbc:mysql://localhost/a", "root", "1234");
		for (int i = 0; i < s.getTodayInvoiceID().size(); i++) {
			s.addListProductintoListBox(s.getTodayInvoiceID().get(i));
		}

	}

}
