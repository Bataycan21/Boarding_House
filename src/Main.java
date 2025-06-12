import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.time.LocalDate; // For date handling, though we'll start with String for simplicity
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Arrays;
import java.util.Vector;

// Represents an Apartment object with its properties.
class Apartment implements Serializable {
    private String apartmentNumber;
    private String tenantName;
    private double rent;
    private boolean occupied;
    private String documentContent;

    public Apartment(String apartmentNumber, String tenantName, double rent, boolean occupied, String documentContent) {
        this.apartmentNumber = apartmentNumber;
        this.tenantName = tenantName;
        this.rent = rent;
        this.occupied = occupied;
        this.documentContent = documentContent;
    }

    // Getters & Setters
    public String getApartmentNumber() { return apartmentNumber; }
    public String getTenantName() { return tenantName; }
    public double getRent() { return rent; }
    public boolean isOccupied() { return occupied; }
    public String getDocumentContent() { return documentContent; }
    public void setTenantName(String tenantName) { this.tenantName = tenantName; }
    public void setRent(double rent) { this.rent = rent; }
    public void setOccupied(boolean occupied) { this.occupied = occupied; }
    public void setDocumentContent(String documentContent) { this.documentContent = documentContent; }

    @Override
    public String toString() {
        String status = occupied ? "Occupied" : "Available";
        String contentInfo = (documentContent != null && !documentContent.trim().isEmpty()) ? "Content: Yes" : "Content: No";
        return String.format("Apt No: %s | Tenant: %s | Rent: $%.2f | Status: %s | %s",
                apartmentNumber, tenantName, rent, status, contentInfo);
    }

    public String toCsvString() {
        String safeDocumentContent = documentContent.replace("\n", "\\n").replace("\r", "");
        return String.join(",", apartmentNumber, tenantName, String.valueOf(rent),
                String.valueOf(occupied), safeDocumentContent);
    }

    public static Apartment fromCsvString(String csv) {
        String[] parts = csv.split(",", 5);
        if (parts.length != 5) return null;
        try {
            return new Apartment(parts[0], parts[1], Double.parseDouble(parts[2]), Boolean.parseBoolean(parts[3]), parts[4].replace("\\n", "\n"));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

// Manages a collection of Apartment objects.
class ApartmentManager {
    private List<Apartment> apartments;
    private static final String FILE_NAME = "apartments.dat";

    public ApartmentManager() {
        this.apartments = new ArrayList<>();
        loadApartments();
        if (apartments.isEmpty()) {
            apartments.add(new Apartment("101", "Steph Curry", 20000.00, true, "Arriving soon."));
        }
    }

    public boolean addApartment(Apartment apartment) {
        if (findApartmentByNumber(apartment.getApartmentNumber()).isPresent()) return false;
        apartments.add(apartment);
        return true;
    }

    public Optional<Apartment> findApartmentByNumber(String apartmentNumber) {
        return apartments.stream().filter(a -> a.getApartmentNumber().equalsIgnoreCase(apartmentNumber)).findFirst();
    }

    public boolean updateApartment(Apartment updatedApartment) {
        Optional<Apartment> existingApartmentOpt = findApartmentByNumber(updatedApartment.getApartmentNumber());
        if (existingApartmentOpt.isPresent()) {
            Apartment existing = existingApartmentOpt.get();
            existing.setTenantName(updatedApartment.getTenantName());
            existing.setRent(updatedApartment.getRent());
            existing.setOccupied(updatedApartment.isOccupied());
            existing.setDocumentContent(updatedApartment.getDocumentContent());
            return true;
        }
        return false;
    }

    public boolean deleteApartment(String apartmentNumber) {
        return apartments.removeIf(a -> a.getApartmentNumber().equalsIgnoreCase(apartmentNumber));
    }

    public List<Apartment> getAllApartments() {
        return new ArrayList<>(apartments);
    }

    public void saveApartments() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (Apartment apt : apartments) {
                writer.println(apt.toCsvString());
            }
        } catch (IOException e) {
            System.err.println("Error saving apartments: " + e.getMessage());
        }
    }

    private void loadApartments() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Apartment apt = Apartment.fromCsvString(line);
                if (apt != null) apartments.add(apt);
            }
        } catch (IOException e) {
            System.err.println("Error loading apartments: " + e.getMessage());
        }
    }
}

// Represents a User object.
class User implements Serializable {
    private String username;
    private String password;
    private String role;

    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // Getters & Setters
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(String role) { this.role = role; }

    public String toCsvString() {
        return String.join(",", username, password, role);
    }

    public static User fromCsvString(String csv) {
        String[] parts = csv.split(",");
        if (parts.length != 3) return null;
        return new User(parts[0], parts[1], parts[2]);
    }
}

// Manages a collection of User objects.
class UserManager {
    private List<User> users;
    private static final String FILE_NAME = "users.dat";

    public UserManager() {
        this.users = new ArrayList<>();
        loadUsers();
        if (users.isEmpty()) {
            users.add(new User("admin", "adminpass", "admin"));
            users.add(new User("user", "password", "regular"));
            users.add(new User("manager", "manage123", "regular"));
        }
    }

    public boolean addUser(User user) {
        if (findUserByUsername(user.getUsername()).isPresent()) return false;
        users.add(user);
        return true;
    }

    public Optional<User> findUserByUsername(String username) {
        return users.stream().filter(u -> u.getUsername().equalsIgnoreCase(username)).findFirst();
    }

    public boolean updateUser(User updatedUser) {
        Optional<User> existingUserOpt = findUserByUsername(updatedUser.getUsername());
        if (existingUserOpt.isPresent()) {
            User existing = existingUserOpt.get();
            existing.setPassword(updatedUser.getPassword());
            existing.setRole(updatedUser.getRole());
            return true;
        }
        return false;
    }

    public boolean deleteUser(String username) {
        return users.removeIf(u -> u.getUsername().equalsIgnoreCase(username));
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }

    public String authenticate(String username, String password) {
        Optional<User> userOpt = findUserByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getPassword().equals(password)) {
                return user.getRole();
            }
        }
        return null;
    }

    public void saveUsers() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (User user : users) {
                writer.println(user.toCsvString());
            }
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }

    private void loadUsers() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                User user = User.fromCsvString(line);
                if (user != null) users.add(user);
            }
        } catch (IOException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
    }
}

// NEW: Represents a ParkingLot object
class ParkingLot implements Serializable {
    private String spotNumber;
    private boolean isReserved;
    private String reservedByTenantName;
    private String reservationDate; // Using String for simplicity, can be LocalDate

    public ParkingLot(String spotNumber, boolean isReserved, String reservedByTenantName, String reservationDate) {
        this.spotNumber = spotNumber;
        this.isReserved = isReserved;
        this.reservedByTenantName = reservedByTenantName;
        this.reservationDate = reservationDate;
    }

    // Getters
    public String getSpotNumber() { return spotNumber; }
    public boolean isReserved() { return isReserved; }
    public String getReservedByTenantName() { return reservedByTenantName; }
    public String getReservationDate() { return reservationDate; }

    // Setters (for updates)
    public void setReserved(boolean reserved) { isReserved = reserved; }
    public void setReservedByTenantName(String reservedByTenantName) { this.reservedByTenantName = reservedByTenantName; }
    public void setReservationDate(String reservationDate) { this.reservationDate = reservationDate; }

    public String toCsvString() {
        return String.join(",", spotNumber,
                String.valueOf(isReserved),
                (reservedByTenantName != null ? reservedByTenantName : ""), // Handle null
                (reservationDate != null ? reservationDate : "")); // Handle null
    }

    public static ParkingLot fromCsvString(String csv) {
        String[] parts = csv.split(",", 4);
        if (parts.length != 4) return null;
        try {
            return new ParkingLot(parts[0],
                    Boolean.parseBoolean(parts[1]),
                    parts[2].isEmpty() ? null : parts[2],
                    parts[3].isEmpty() ? null : parts[3]);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

// NEW: Manages a collection of ParkingLot objects
class ParkingLotManager {
    private List<ParkingLot> parkingLots;
    private static final String FILE_NAME = "parking_lots.dat";

    public ParkingLotManager() {
        this.parkingLots = new ArrayList<>();
        loadParkingLots();
        if (parkingLots.isEmpty()) {
            // Add some dummy data if file is empty
            parkingLots.add(new ParkingLot("P01", false, null, null));
            parkingLots.add(new ParkingLot("P02", true, "Alice Smith", LocalDate.now().toString()));
            parkingLots.add(new ParkingLot("P03", false, null, null));
            parkingLots.add(new ParkingLot("P04", true, "Bob Johnson", LocalDate.now().plusDays(2).toString()));
            parkingLots.add(new ParkingLot("P05", false, null, null));
        }
    }

    public boolean addSpot(ParkingLot spot) {
        if (findSpotByNumber(spot.getSpotNumber()).isPresent()) return false;
        parkingLots.add(spot);
        return true;
    }

    public Optional<ParkingLot> findSpotByNumber(String spotNumber) {
        return parkingLots.stream().filter(s -> s.getSpotNumber().equalsIgnoreCase(spotNumber)).findFirst();
    }

    public boolean reserveSpot(String spotNumber, String tenantName, String reservationDate) {
        Optional<ParkingLot> spotOpt = findSpotByNumber(spotNumber);
        if (spotOpt.isPresent()) {
            ParkingLot spot = spotOpt.get();
            if (!spot.isReserved()) {
                spot.setReserved(true);
                spot.setReservedByTenantName(tenantName);
                spot.setReservationDate(reservationDate);
                return true;
            }
        }
        return false;
    }

    public boolean cancelReservation(String spotNumber) {
        Optional<ParkingLot> spotOpt = findSpotByNumber(spotNumber);
        if (spotOpt.isPresent()) {
            ParkingLot spot = spotOpt.get();
            if (spot.isReserved()) {
                spot.setReserved(false);
                spot.setReservedByTenantName(null);
                spot.setReservationDate(null);
                return true;
            }
        }
        return false;
    }

    public boolean deleteSpot(String spotNumber) {
        return parkingLots.removeIf(s -> s.getSpotNumber().equalsIgnoreCase(spotNumber));
    }

    public List<ParkingLot> getAllParkingLots() {
        return new ArrayList<>(parkingLots);
    }

    public void saveParkingLots() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (ParkingLot spot : parkingLots) {
                writer.println(spot.toCsvString());
            }
        } catch (IOException e) {
            System.err.println("Error saving parking lots: " + e.getMessage());
        }
    }

    private void loadParkingLots() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                ParkingLot spot = ParkingLot.fromCsvString(line);
                if (spot != null) parkingLots.add(spot);
            }
        } catch (IOException e) {
            System.err.println("Error loading parking lots: " + e.getMessage());
        }
    }
}


// Login screen for the Apartment Management System.
class LoginScreen extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private UserManager userManager;

    public LoginScreen() {
        userManager = new UserManager();
        setTitle("Login - Apartment Management System");
        setSize(350, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel loginPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        loginPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        usernameField = new JTextField(15);
        passwordField = new JPasswordField(15);
        loginButton = new JButton("Login");
        loginPanel.add(new JLabel("Username:"));
        loginPanel.add(usernameField);
        loginPanel.add(new JLabel("Password:"));
        loginPanel.add(passwordField);
        loginPanel.add(new JLabel(""));
        loginPanel.add(loginButton);
        add(loginPanel, BorderLayout.CENTER);

        loginButton.addActionListener(e -> attemptLogin());
        passwordField.addActionListener(e -> attemptLogin());
    }

    private void attemptLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String userRole = userManager.authenticate(username, password);

        if (userRole != null) {
            JOptionPane.showMessageDialog(this, "Login Successful! Role: " + userRole, "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            // Pass username, role, and the manager instance to the main GUI
            SwingUtilities.invokeLater(() -> new ApartmentManagementGUI(username, userRole, userManager).setVisible(true));
        } else {
            JOptionPane.showMessageDialog(this, "Invalid Username or Password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
        }
        Arrays.fill(passwordField.getPassword(), ' ');
    }
}

/**
 * The main GUI class for the Apartment Management System.
 * This class should be public to be the main entry point.
 */
class ApartmentManagementGUI extends JFrame {
    private ApartmentManager apartmentManager;
    private UserManager userManager;
    private ParkingLotManager parkingLotManager; // NEW: Parking Lot Manager
    private String currentUsername;
    private String currentUserRole;

    // GUI Components
    private JTabbedPane mainTabbedPane;
    private JTextField aptNumberField, tenantNameField, rentField, userUsernameField;
    private JPasswordField userPasswordField;
    private JCheckBox occupiedCheckBox;
    private JTextArea documentContentArea;
    private JComboBox<String> userRoleComboBox;
    private JButton addButton, updateButton, deleteButton, listApartmentsButton, bookApartmentButton;
    private JButton addUserButton, updateUserButton, deleteUserButton, listUsersButton;
    private JTable apartmentTable, userTable;
    private DefaultTableModel apartmentTableModel, userTableModel;
    private JLabel apartmentMessageLabel, userMessageLabel;

    // NEW: Parking Lot GUI Components
    private JTextField spotNumberField, reservedByTenantNameField, reservationDateField;
    private JCheckBox isReservedCheckBox;
    private JButton addSpotButton, reserveSpotButton, cancelReservationButton, deleteSpotButton, refreshParkingListButton;
    private JTable parkingTable;
    private DefaultTableModel parkingTableModel;
    private JLabel parkingMessageLabel;


    public ApartmentManagementGUI(String username, String userRole, UserManager userManager) {
        this.currentUsername = username;
        this.currentUserRole = userRole;
        this.userManager = userManager;
        this.apartmentManager = new ApartmentManager();
        this.parkingLotManager = new ParkingLotManager(); // NEW: Initialize parking manager

        setTitle("Apartment Management System - Logged in as: " + currentUsername + " (" + currentUserRole + ")");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Changed for custom close handling
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Custom window closing behavior
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Delegate to the logout logic for consistency
                performLogout();
            }
        });

        // --- Header Panel (for title and logout button) ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Apartment Management System", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        JButton logoutButton = new JButton("Log Out");
        logoutButton.addActionListener(e -> performLogout());

        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // Align button to the right
        logoutPanel.add(logoutButton);
        headerPanel.add(logoutPanel, BorderLayout.EAST);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Padding for header
        add(headerPanel, BorderLayout.NORTH); // Add header to the top of the frame

        // --- Main Tabbed Pane ---
        mainTabbedPane = new JTabbedPane();
        mainTabbedPane.addTab("Apartment Management", createApartmentManagementPanel());
        mainTabbedPane.addTab("Parking Management", createParkingManagementPanel()); // NEW: Add Parking tab

        if ("admin".equalsIgnoreCase(currentUserRole)) {
            mainTabbedPane.addTab("User Management", createUserManagementPanel());
        }

        add(mainTabbedPane, BorderLayout.CENTER); // Main content (tabs) in the center

        // Initial list loading for all tabs
        listAllApartments();
        listAllParkingLots(); // NEW: List all parking lots on startup
        if ("admin".equalsIgnoreCase(currentUserRole)) {
            listAllUsers();
        }
    }

    private void performLogout() {
        int confirm = JOptionPane.showConfirmDialog(ApartmentManagementGUI.this,
                "Do you want to save changes before logging out?", "Log Out Confirmation", JOptionPane.YES_NO_CANCEL_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            apartmentManager.saveApartments();
            parkingLotManager.saveParkingLots(); // NEW: Save parking data
            if ("admin".equalsIgnoreCase(currentUserRole)) {
                userManager.saveUsers();
            }
            JOptionPane.showMessageDialog(ApartmentManagementGUI.this, "Data saved successfully. Logging out.");
            dispose(); // Close current GUI
            SwingUtilities.invokeLater(() -> new LoginScreen().setVisible(true)); // Open login screen
        } else if (confirm == JOptionPane.NO_OPTION) {
            dispose(); // Close current GUI without saving
            SwingUtilities.invokeLater(() -> new LoginScreen().setVisible(true)); // Open login screen
        }
        // If CANCEL, do nothing (stay on current screen)
    }


    private JPanel createApartmentManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Create a container for the top section (details input + document content + message)
        JPanel topSectionPanel = new JPanel(new BorderLayout(10, 10)); // Use BorderLayout for top section

        // Top form panel
        JPanel detailsInputPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        detailsInputPanel.setBorder(BorderFactory.createTitledBorder("Apartment Details"));
        aptNumberField = new JTextField(10);
        tenantNameField = new JTextField(20);
        rentField = new JTextField(10);
        occupiedCheckBox = new JCheckBox("Occupied");

        // Add components to the details input panel
        detailsInputPanel.add(new JLabel("Apartment Number:"));
        detailsInputPanel.add(aptNumberField);
        detailsInputPanel.add(new JLabel("Tenant Name:"));
        detailsInputPanel.add(tenantNameField);
        detailsInputPanel.add(new JLabel("Rent:"));
        detailsInputPanel.add(rentField);

        // Create a separate panel for the occupied checkbox to control its layout
        // Add a top border to push the content down slightly
        JPanel occupiedPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        occupiedPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0)); // Top padding of 5 pixels
        occupiedPanel.add(occupiedCheckBox);

        JLabel statusLabel = new JLabel("Status:");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0)); // Top padding for the label too

        detailsInputPanel.add(statusLabel);
        detailsInputPanel.add(occupiedPanel); // Add the panel with the checkbox

        topSectionPanel.add(detailsInputPanel, BorderLayout.NORTH); // Add detailsInputPanel to topSectionPanel's NORTH

        // Center text area
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10)); // This was the previous centerPanel
        documentContentArea = new JTextArea(8, 30);
        documentContentArea.setLineWrap(true);
        documentContentArea.setWrapStyleWord(true);
        JScrollPane docScrollPane = new JScrollPane(documentContentArea);
        docScrollPane.setBorder(BorderFactory.createTitledBorder("Apartment Document Content"));
        centerPanel.add(docScrollPane, BorderLayout.CENTER);
        apartmentMessageLabel = new JLabel("System messages will appear here.", SwingConstants.CENTER);
        apartmentMessageLabel.setForeground(Color.BLUE);
        centerPanel.add(apartmentMessageLabel, BorderLayout.SOUTH);

        topSectionPanel.add(centerPanel, BorderLayout.CENTER); // Add this centerPanel to topSectionPanel's CENTER

        panel.add(topSectionPanel, BorderLayout.NORTH); // Add the combined topSectionPanel to main panel's NORTH


        // Bottom panel with table and buttons
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));

        String[] apartmentColumnNames = {"Apt No", "Tenant", "Rent", "Status", "Document Info"};
        apartmentTableModel = new DefaultTableModel(apartmentColumnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        apartmentTable = new JTable(apartmentTableModel);
        apartmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane listScrollPane = new JScrollPane(apartmentTable);
        listScrollPane.setBorder(BorderFactory.createTitledBorder("Apartment List"));
        bottomPanel.add(listScrollPane, BorderLayout.CENTER); // Table will now take center space of this bottom panel

        JPanel bottomButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        addButton = new JButton("Add Apartment");
        updateButton = new JButton("Update Apartment");
        deleteButton = new JButton("Delete Apartment");
        listApartmentsButton = new JButton("Refresh List");
        bookApartmentButton = new JButton("Book Apartment");
        JButton clearApartmentFieldsButton = new JButton("Clear Fields");

        bottomButtonsPanel.add(addButton);
        bottomButtonsPanel.add(updateButton);
        bottomButtonsPanel.add(deleteButton);
        bottomButtonsPanel.add(listApartmentsButton);
        if ("regular".equalsIgnoreCase(currentUserRole)) {
            bottomButtonsPanel.add(bookApartmentButton);
        }
        bottomButtonsPanel.add(clearApartmentFieldsButton);
        bottomPanel.add(bottomButtonsPanel, BorderLayout.SOUTH); // Buttons will be at the bottom of this bottom panel

        panel.add(bottomPanel, BorderLayout.CENTER); // Main panel now has topSection in NORTH and bottomPanel (with table) in CENTER

        // Action Listeners
        addButton.addActionListener(e -> addApartment());
        updateButton.addActionListener(e -> updateApartment());
        deleteButton.addActionListener(e -> deleteApartment());
        listApartmentsButton.addActionListener(e -> listAllApartments());
        bookApartmentButton.addActionListener(e -> bookApartment());
        clearApartmentFieldsButton.addActionListener(e -> clearApartmentFields());
        apartmentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && apartmentTable.getSelectedRow() != -1) {
                loadApartmentDetailsFromTable();
            }
        });

        // Permissions
        if ("regular".equalsIgnoreCase(currentUserRole)) {
            addButton.setEnabled(false);
            updateButton.setEnabled(false);
            deleteButton.setEnabled(false);
            aptNumberField.setEditable(false);
            rentField.setEditable(false);
            occupiedCheckBox.setEnabled(false);
            documentContentArea.setEditable(false);
        }

        return panel;
    }

    private JPanel createUserManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // New topSectionPanel for User Management
        JPanel topSectionPanel = new JPanel(new BorderLayout(10, 10));

        // Top Panel for User Details Input
        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createTitledBorder("User Details"));
        userUsernameField = new JTextField(15);
        userPasswordField = new JPasswordField(15);
        userRoleComboBox = new JComboBox<>(new String[]{"regular", "admin"});
        inputPanel.add(new JLabel("Username:"));
        inputPanel.add(userUsernameField);
        inputPanel.add(new JLabel("Password:"));
        inputPanel.add(userPasswordField);
        inputPanel.add(new JLabel("Role:"));
        inputPanel.add(userRoleComboBox);
        topSectionPanel.add(inputPanel, BorderLayout.NORTH); // Add inputPanel to topSectionPanel's NORTH

        // Center Panel for message Label
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10)); // This was the previous centerPanel
        userMessageLabel = new JLabel("System messages for users will appear here.", SwingConstants.CENTER);
        userMessageLabel.setForeground(Color.BLUE);
        centerPanel.add(userMessageLabel, BorderLayout.CENTER);
        topSectionPanel.add(centerPanel, BorderLayout.CENTER); // Add this centerPanel to topSectionPanel's CENTER

        panel.add(topSectionPanel, BorderLayout.NORTH); // Add the combined topSectionPanel to main panel's NORTH

        // Bottom Panel for the Table and action buttons
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        String[] userColumnNames = {"Username", "Role"};
        userTableModel = new DefaultTableModel(userColumnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        userTable = new JTable(userTableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("User List"));
        bottomPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        addUserButton = new JButton("Add User");
        updateUserButton = new JButton("Update User");
        deleteUserButton = new JButton("Delete User");
        listUsersButton = new JButton("Refresh List");
        JButton clearUserFieldsButton = new JButton("Clear User Fields");

        bottomButtonPanel.add(addUserButton);
        bottomButtonPanel.add(updateUserButton);
        bottomButtonPanel.add(deleteUserButton);
        bottomButtonPanel.add(listUsersButton);
        bottomButtonPanel.add(clearUserFieldsButton);
        bottomPanel.add(bottomButtonPanel, BorderLayout.SOUTH);

        panel.add(bottomPanel, BorderLayout.CENTER); // Main panel now has topSection in NORTH and bottomPanel (with table) in CENTER

        // Action Listeners
        addUserButton.addActionListener(e -> addUser());
        clearUserFieldsButton.addActionListener(e -> clearUserFields());
        updateUserButton.addActionListener(e -> updateUser());
        deleteUserButton.addActionListener(e -> deleteUser());
        listUsersButton.addActionListener(e -> listAllUsers());
        userTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && userTable.getSelectedRow() != -1) {
                loadUserDetailsFromTable();
            }
        });

        return panel;
    }

    // NEW: Method to create the Parking Management Panel
    private JPanel createParkingManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Top section for input fields and message
        JPanel topSectionPanel = new JPanel(new BorderLayout(10, 10));

        // Input Panel for Parking Spot Details
        JPanel detailsInputPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        detailsInputPanel.setBorder(BorderFactory.createTitledBorder("Parking Spot Details"));
        spotNumberField = new JTextField(10);
        reservedByTenantNameField = new JTextField(20);
        reservationDateField = new JTextField(10); // Format:YYYY-MM-DD
        isReservedCheckBox = new JCheckBox("Reserved");

        detailsInputPanel.add(new JLabel("Spot Number:"));
        detailsInputPanel.add(spotNumberField);
        detailsInputPanel.add(new JLabel("Reserved By:"));
        detailsInputPanel.add(reservedByTenantNameField);
        detailsInputPanel.add(new JLabel("Reservation Date (YYYY-MM-DD):"));
        detailsInputPanel.add(reservationDateField);
        detailsInputPanel.add(new JLabel("Status:"));
        detailsInputPanel.add(isReservedCheckBox);

        topSectionPanel.add(detailsInputPanel, BorderLayout.NORTH);

        parkingMessageLabel = new JLabel("Parking system messages will appear here.", SwingConstants.CENTER);
        parkingMessageLabel.setForeground(Color.BLUE);
        topSectionPanel.add(parkingMessageLabel, BorderLayout.CENTER);

        panel.add(topSectionPanel, BorderLayout.NORTH);

        // Bottom section for table and buttons
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));

        String[] parkingColumnNames = {"Spot No", "Reserved", "Reserved By", "Reservation Date"};
        parkingTableModel = new DefaultTableModel(parkingColumnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        parkingTable = new JTable(parkingTableModel);
        parkingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane listScrollPane = new JScrollPane(parkingTable);
        listScrollPane.setBorder(BorderFactory.createTitledBorder("Parking Spot List"));
        bottomPanel.add(listScrollPane, BorderLayout.CENTER);

        JPanel bottomButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        addSpotButton = new JButton("Add Spot");
        reserveSpotButton = new JButton("Reserve Spot");
        cancelReservationButton = new JButton("Cancel Reservation");
        deleteSpotButton = new JButton("Delete Spot");
        refreshParkingListButton = new JButton("Refresh List");
        JButton clearParkingFieldsButton = new JButton("Clear Fields");

        bottomButtonsPanel.add(addSpotButton);
        bottomButtonsPanel.add(reserveSpotButton);
        bottomButtonsPanel.add(cancelReservationButton);
        bottomButtonsPanel.add(deleteSpotButton);
        bottomButtonsPanel.add(refreshParkingListButton);
        bottomButtonsPanel.add(clearParkingFieldsButton);
        bottomPanel.add(bottomButtonsPanel, BorderLayout.SOUTH);

        panel.add(bottomPanel, BorderLayout.CENTER);

        // Action Listeners
        addSpotButton.addActionListener(e -> addParkingSpot());
        reserveSpotButton.addActionListener(e -> reserveParkingSpot());
        cancelReservationButton.addActionListener(e -> cancelParkingSpotReservation());
        deleteSpotButton.addActionListener(e -> deleteParkingSpot());
        refreshParkingListButton.addActionListener(e -> listAllParkingLots());
        clearParkingFieldsButton.addActionListener(e -> clearParkingFields());
        parkingTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && parkingTable.getSelectedRow() != -1) {
                loadParkingDetailsFromTable();
            }
        });

        // Permissions for Parking Lot Management
        if ("regular".equalsIgnoreCase(currentUserRole)) {
            addSpotButton.setEnabled(false);
            deleteSpotButton.setEnabled(false);
            spotNumberField.setEditable(false);
            // Regular users can reserve/cancel but usually for themselves, so disable direct editing of 'reserved by' for safety
            reservedByTenantNameField.setEditable(false);
            // reservationDateField.setEditable(false); // This will be enabled for input now
            isReservedCheckBox.setEnabled(false); // Status is set by reserve/cancel actions, not direct check
        }

        return panel;
    }


    private void clearApartmentFields() {
        aptNumberField.setText("");
        tenantNameField.setText("");
        rentField.setText("");
        occupiedCheckBox.setSelected(false);
        documentContentArea.setText("");
        apartmentTable.clearSelection();
        displayApartmentMessage("Apartment fields cleared.");
    }

    private void loadApartmentDetailsFromTable() {
        int row = apartmentTable.getSelectedRow();
        if (row >= 0) {
            String aptNum = (String) apartmentTableModel.getValueAt(row, 0);
            apartmentManager.findApartmentByNumber(aptNum).ifPresent(apt -> {
                aptNumberField.setText(apt.getApartmentNumber());
                tenantNameField.setText(apt.getTenantName());
                rentField.setText(String.valueOf(apt.getRent()));
                occupiedCheckBox.setSelected(apt.isOccupied());
                documentContentArea.setText(apt.getDocumentContent());
                displayApartmentMessage("Details for apartment " + aptNum + " loaded.");
            });
        }
    }

    private void addApartment() {
        if (!"admin".equalsIgnoreCase(currentUserRole)) {
            displayApartmentMessage("Permission Denied: Only administrators can add apartments.");
            return;
        }
        try {
            String aptNum = aptNumberField.getText().trim();
            if (aptNum.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Apartment Number cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Apartment newApt = new Apartment(aptNum, tenantNameField.getText().trim(), Double.parseDouble(rentField.getText().trim()), occupiedCheckBox.isSelected(), documentContentArea.getText());
            if (apartmentManager.addApartment(newApt)) {
                displayApartmentMessage("Apartment " + aptNum + " added successfully.");
                clearApartmentFields();
                listAllApartments();
            } else {
                JOptionPane.showMessageDialog(this, "Apartment " + aptNum + " already exists.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid rent value.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateApartment() {
        if (!"admin".equalsIgnoreCase(currentUserRole)) {
            displayApartmentMessage("Permission Denied: Only administrators can update apartments.");
            return;
        }
        try {
            String aptNum = aptNumberField.getText().trim();
            if (aptNum.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select an apartment to update.", "Selection Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Apartment updatedApt = new Apartment(aptNum, tenantNameField.getText().trim(), Double.parseDouble(rentField.getText().trim()), occupiedCheckBox.isSelected(), documentContentArea.getText());
            if (apartmentManager.updateApartment(updatedApt)) {
                displayApartmentMessage("Apartment " + aptNum + " updated successfully.");
                clearApartmentFields();
                listAllApartments();
            } else {
                JOptionPane.showMessageDialog(this, "Apartment " + aptNum + " not found for update.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid rent value.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteApartment() {
        if (!"admin".equalsIgnoreCase(currentUserRole)) {
            displayApartmentMessage("Permission Denied: Only administrators can delete apartments.");
            return;
        }
        int row = apartmentTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an apartment to delete.", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String aptNum = (String) apartmentTableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete apartment " + aptNum + "?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (apartmentManager.deleteApartment(aptNum)) {
                displayApartmentMessage("Apartment " + aptNum + " deleted.");
                clearApartmentFields();
                listAllApartments();
            }
        }
    }

    private void bookApartment() {
        if (!"regular".equalsIgnoreCase(currentUserRole)) {
            displayApartmentMessage("Permission Denied: Only regular users can book apartments.");
            return;
        }
        int row = apartmentTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an available apartment to book.", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String aptNum = (String) apartmentTableModel.getValueAt(row, 0);
        Optional<Apartment> aptOpt = apartmentManager.findApartmentByNumber(aptNum);
        if (aptOpt.isPresent()) {
            Apartment apt = aptOpt.get();
            if (apt.isOccupied()) {
                JOptionPane.showMessageDialog(this, "This apartment is already occupied.", "Booking Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // For booking, automatically set tenant name to current username
            String newTenantName = currentUsername;
            if (newTenantName != null && !newTenantName.trim().isEmpty()) {
                apt.setTenantName(newTenantName.trim());
                apt.setOccupied(true);
                apartmentManager.updateApartment(apt);
                displayApartmentMessage("Apartment " + aptNum + " booked successfully by " + newTenantName);
                listAllApartments();
                clearApartmentFields();
            } else {
                JOptionPane.showMessageDialog(this, "Tenant name cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void listAllApartments() {
        apartmentTableModel.setRowCount(0);
        for (Apartment apt : apartmentManager.getAllApartments()) {
            Vector<Object> row = new Vector<>();
            row.add(apt.getApartmentNumber());
            row.add(apt.getTenantName());
            row.add(String.format("%.2f", apt.getRent()));
            row.add(apt.isOccupied() ? "Occupied" : "Available");
            row.add(apt.getDocumentContent() != null && !apt.getDocumentContent().trim().isEmpty() ? "Yes" : "No");
            apartmentTableModel.addRow(row);
        }
    }

    private void displayApartmentMessage(String message) {
        apartmentMessageLabel.setText(message);
    }

    private void clearUserFields() {
        userUsernameField.setText("");
        userPasswordField.setText("");
        userRoleComboBox.setSelectedIndex(0);
        userTable.clearSelection();
        userUsernameField.setEditable(true);
        displayUserMessage("User fields cleared.");
    }

    private void loadUserDetailsFromTable() {
        int row = userTable.getSelectedRow();
        if (row >= 0) {
            String username = (String) userTableModel.getValueAt(row, 0);
            userManager.findUserByUsername(username).ifPresent(user -> {
                userUsernameField.setText(user.getUsername());
                userPasswordField.setText(""); // Don't load password for security
                userRoleComboBox.setSelectedItem(user.getRole());
                userUsernameField.setEditable(false); // Username should not be editable for existing users
                displayUserMessage("Details for user " + username + " loaded.");
            });
        }
    }

    private void addUser() {
        String username = userUsernameField.getText().trim();
        String password = new String(userPasswordField.getPassword()).trim();
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and Password cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        User newUser = new User(username, password, (String) userRoleComboBox.getSelectedItem());
        if (userManager.addUser(newUser)) {
            displayUserMessage("User " + username + " added successfully.");
            clearUserFields();
            listAllUsers();
        } else {
            JOptionPane.showMessageDialog(this, "User " + username + " already exists.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateUser() {
        String username = userUsernameField.getText().trim();
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a user to update.", "Selection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Optional<User> userOpt = userManager.findUserByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String password = new String(userPasswordField.getPassword()).trim();
            if (!password.isEmpty()) { // Only update password if a new one is entered
                user.setPassword(password);
            }
            user.setRole((String) userRoleComboBox.getSelectedItem());
            if (userManager.updateUser(user)) {
                displayUserMessage("User " + username + " updated successfully.");
                clearUserFields();
                listAllUsers();
            } else {
                JOptionPane.showMessageDialog(this, "User " + username + " not found for update.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteUser() {
        int row = userTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user to delete.", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String username = (String) userTableModel.getValueAt(row, 0);
        if (username.equalsIgnoreCase(currentUsername)) {
            JOptionPane.showMessageDialog(this, "You cannot delete your own account.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete user " + username + "?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (userManager.deleteUser(username)) {
                displayUserMessage("User " + username + " deleted.");
                clearUserFields();
                listAllUsers();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete user " + username + ".", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void listAllUsers() {
        userTableModel.setRowCount(0);
        for (User user : userManager.getAllUsers()) {
            Vector<Object> row = new Vector<>();
            row.add(user.getUsername());
            row.add(user.getRole());
            userTableModel.addRow(row);
        }
        displayUserMessage("User list refreshed. Total users: " + userManager.getAllUsers().size());
    }

    private void displayUserMessage(String message) {
        userMessageLabel.setText(message);
    }

    // NEW: Parking Lot Management Methods
    private void clearParkingFields() {
        spotNumberField.setText("");
        reservedByTenantNameField.setText("");
        reservationDateField.setText("");
        isReservedCheckBox.setSelected(false);
        parkingTable.clearSelection();
        spotNumberField.setEditable(true); // Re-enable for new entry
        if ("regular".equalsIgnoreCase(currentUserRole)) {
            // Restore permissions for regular user after clearing
            reservedByTenantNameField.setEditable(false);
            // reservationDateField.setEditable(true); // Always editable for input when reserving
            isReservedCheckBox.setEnabled(false);
        } else { // Admin can edit all fields
            reservedByTenantNameField.setEditable(true);
            reservationDateField.setEditable(true);
            isReservedCheckBox.setEnabled(true);
        }
        displayParkingMessage("Parking fields cleared.");
    }

    private void loadParkingDetailsFromTable() {
        int row = parkingTable.getSelectedRow();
        if (row >= 0) {
            String spotNum = (String) parkingTableModel.getValueAt(row, 0);
            parkingLotManager.findSpotByNumber(spotNum).ifPresent(spot -> {
                spotNumberField.setText(spot.getSpotNumber());
                isReservedCheckBox.setSelected(spot.isReserved());
                reservedByTenantNameField.setText(spot.getReservedByTenantName() != null ? spot.getReservedByTenantName() : "");
                reservationDateField.setText(spot.getReservationDate() != null ? spot.getReservationDate() : "");

                spotNumberField.setEditable(false); // Can't edit spot number of existing spot

                // Adjust permissions based on role
                if ("regular".equalsIgnoreCase(currentUserRole)) {
                    reservedByTenantNameField.setEditable(false);
                    // reservationDateField.setEditable(true); // This field is now for customer input
                    isReservedCheckBox.setEnabled(false);
                } else { // Admin can edit these fields for existing spots
                    reservedByTenantNameField.setEditable(true);
                    reservationDateField.setEditable(true);
                    isReservedCheckBox.setEnabled(true);
                }
                displayParkingMessage("Details for spot " + spotNum + " loaded.");
            });
        }
    }

    private void addParkingSpot() {
        if (!"admin".equalsIgnoreCase(currentUserRole)) {
            displayParkingMessage("Permission Denied: Only administrators can add parking spots.");
            return;
        }
        String spotNum = spotNumberField.getText().trim();
        if (spotNum.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Spot Number cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // For adding, assume it's initially not reserved unless specified
        boolean isReserved = isReservedCheckBox.isSelected();
        String reservedBy = isReserved ? reservedByTenantNameField.getText().trim() : null;
        String resDate = isReserved ? reservationDateField.getText().trim() : null;

        if (isReserved && reservedBy.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Reserved By field cannot be empty if spot is reserved.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (isReserved && resDate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Reservation Date cannot be empty if spot is reserved.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Basic date format validation
        if (isReserved && !resDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
            JOptionPane.showMessageDialog(this, "Reservation Date must be in YYYY-MM-DD format.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            if (isReserved) LocalDate.parse(resDate); // Validate date format
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format for Reservation Date. Please use YYYY-MM-DD.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }


        ParkingLot newSpot = new ParkingLot(spotNum, isReserved, reservedBy, resDate);
        if (parkingLotManager.addSpot(newSpot)) {
            displayParkingMessage("Parking spot " + spotNum + " added successfully.");
            clearParkingFields();
            listAllParkingLots();
        } else {
            JOptionPane.showMessageDialog(this, "Parking spot " + spotNum + " already exists.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void reserveParkingSpot() {
        int row = parkingTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a parking spot to reserve.", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String spotNum = (String) parkingTableModel.getValueAt(row, 0);
        Optional<ParkingLot> spotOpt = parkingLotManager.findSpotByNumber(spotNum);

        if (spotOpt.isPresent()) {
            ParkingLot spot = spotOpt.get();
            if (spot.isReserved()) {
                JOptionPane.showMessageDialog(this, "Spot " + spotNum + " is already reserved by " + spot.getReservedByTenantName() + ".", "Reservation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String tenantToReserve = currentUsername; // Default to logged-in user
            String reservationDateStr = "";

            // Prompt for date input
            reservationDateStr = JOptionPane.showInputDialog(this,
                    "Enter reservation date for spot " + spotNum + " (YYYY-MM-DD):",
                    LocalDate.now().toString()); // Pre-fill with current date

            if (reservationDateStr == null || reservationDateStr.trim().isEmpty()) {
                // User cancelled or left empty
                displayParkingMessage("Reservation cancelled by user.");
                return;
            }
            reservationDateStr = reservationDateStr.trim();

            // Validate date format
            if (!reservationDateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                JOptionPane.showMessageDialog(this, "Reservation Date must be in YYYY-MM-DD format.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                LocalDate.parse(reservationDateStr); // Further validate date
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Invalid date format for Reservation Date. Please use YYYY-MM-DD.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (parkingLotManager.reserveSpot(spotNum, tenantToReserve, reservationDateStr)) {
                displayParkingMessage("Spot " + spotNum + " reserved by " + tenantToReserve + " for " + reservationDateStr + ".");
                listAllParkingLots();
                clearParkingFields();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to reserve spot " + spotNum + ".", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void cancelParkingSpotReservation() {
        int row = parkingTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a parking spot to cancel its reservation.", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String spotNum = (String) parkingTableModel.getValueAt(row, 0);
        Optional<ParkingLot> spotOpt = parkingLotManager.findSpotByNumber(spotNum);

        if (spotOpt.isPresent()) {
            ParkingLot spot = spotOpt.get();
            if (!spot.isReserved()) {
                JOptionPane.showMessageDialog(this, "Spot " + spotNum + " is not currently reserved.", "Cancellation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Regular users can only cancel their own reservations
            if ("regular".equalsIgnoreCase(currentUserRole) && !spot.getReservedByTenantName().equalsIgnoreCase(currentUsername)) {
                JOptionPane.showMessageDialog(this, "Permission Denied: You can only cancel your own parking reservations.", "Permission Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this, "Cancel reservation for spot " + spotNum + " (Reserved by: " + spot.getReservedByTenantName() + ")?", "Confirm Cancellation", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (parkingLotManager.cancelReservation(spotNum)) {
                    displayParkingMessage("Reservation for spot " + spotNum + " cancelled.");
                    listAllParkingLots();
                    clearParkingFields();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to cancel reservation for spot " + spotNum + ".", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void deleteParkingSpot() {
        if (!"admin".equalsIgnoreCase(currentUserRole)) {
            displayParkingMessage("Permission Denied: Only administrators can delete parking spots.");
            return;
        }
        int row = parkingTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a parking spot to delete.", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String spotNum = (String) parkingTableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete parking spot " + spotNum + "?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (parkingLotManager.deleteSpot(spotNum)) {
                displayParkingMessage("Parking spot " + spotNum + " deleted.");
                clearParkingFields();
                listAllParkingLots();
            }
        }
    }

    private void listAllParkingLots() {
        parkingTableModel.setRowCount(0);
        for (ParkingLot spot : parkingLotManager.getAllParkingLots()) {
            Vector<Object> row = new Vector<>();
            row.add(spot.getSpotNumber());
            row.add(spot.isReserved() ? "Yes" : "No");
            row.add(spot.getReservedByTenantName() != null ? spot.getReservedByTenantName() : "N/A");
            row.add(spot.getReservationDate() != null ? spot.getReservationDate() : "N/A");
            parkingTableModel.addRow(row);
        }
    }

    private void displayParkingMessage(String message) {
        parkingMessageLabel.setText(message);
    }


    public static void main(String[] args) {
        // Ensures the Swing GUI is created and updated on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new LoginScreen().setVisible(true));
    }
}