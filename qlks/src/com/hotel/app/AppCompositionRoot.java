package com.hotel.app;

import com.hotel.booking.application.BookingRepository;
import com.hotel.booking.application.CreateBookingUseCase;
import com.hotel.booking.application.CustomerRepository;
import com.hotel.booking.data.InMemoryBookingRepository;
import com.hotel.booking.data.InMemoryCustomerRepository;
import com.hotel.booking.data.JdbcBookingRepository;
import com.hotel.booking.data.JdbcCustomerRepository;
import com.hotel.booking.ui.BookingPanel;
import com.hotel.common.application.NoOpTransactionManager;
import com.hotel.common.application.TransactionManager;
import com.hotel.common.data.ConnectionProvider;
import com.hotel.common.data.JdbcTransactionManager;
import com.hotel.common.data.MySqlConnectionProvider;
import com.hotel.checkout.application.CalculateCheckoutUseCase;
import com.hotel.checkout.application.CheckoutUseCase;
import com.hotel.checkout.application.InvoiceRepository;
import com.hotel.checkout.data.InMemoryInvoiceRepository;
import com.hotel.checkout.data.JdbcInvoiceRepository;
import com.hotel.checkout.ui.CheckoutPanel;
import com.hotel.revenue.application.GetRevenueReportUseCase;
import com.hotel.revenue.ui.RevenuePanel;
import com.hotel.services.application.AddServiceToBookingUseCase;
import com.hotel.services.application.ListAvailableServicesUseCase;
import com.hotel.services.application.ServiceRepository;
import com.hotel.services.application.ServiceUsageRepository;
import com.hotel.services.data.InMemoryServiceRepository;
import com.hotel.services.data.InMemoryServiceUsageRepository;
import com.hotel.services.data.JdbcServiceRepository;
import com.hotel.services.data.JdbcServiceUsageRepository;
import com.hotel.services.ui.ServicesPanel;
import com.hotel.rooms.application.GetRoomDetailUseCase;
import com.hotel.rooms.application.ListRoomsUseCase;
import com.hotel.rooms.application.UpdateRoomStatusUseCase;
import com.hotel.rooms.application.RoomRepository;
import com.hotel.rooms.data.InMemoryRoomRepository;
import com.hotel.rooms.data.JdbcRoomRepository;
import com.hotel.rooms.ui.RoomsPanel;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class AppCompositionRoot {
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;
    private final ServiceRepository serviceRepository;
    private final ServiceUsageRepository serviceUsageRepository;
    private final InvoiceRepository invoiceRepository;
    private final ConnectionProvider connectionProvider;
    private final TransactionManager transactionManager;

    private final ListRoomsUseCase listRoomsUseCase;
    private final GetRoomDetailUseCase getRoomDetailUseCase;
    private final UpdateRoomStatusUseCase updateRoomStatusUseCase;
    private final CreateBookingUseCase createBookingUseCase;
    private final ListAvailableServicesUseCase listAvailableServicesUseCase;
    private final AddServiceToBookingUseCase addServiceToBookingUseCase;
    private final com.hotel.services.application.ListActiveBookingsUseCase servicesListActiveBookingsUseCase;
    private final com.hotel.checkout.application.ListActiveBookingsUseCase checkoutListActiveBookingsUseCase;
    private final CalculateCheckoutUseCase calculateCheckoutUseCase;
    private final CheckoutUseCase checkoutUseCase;
    private final GetRevenueReportUseCase getRevenueReportUseCase;

    public AppCompositionRoot() {
        Properties props = loadProperties();
        boolean useMySql = Boolean.parseBoolean(props.getProperty("db.enabled", "false"));

        if (useMySql) {
            MySqlConnectionProvider mysqlProvider = MySqlConnectionProvider.fromProperties(props);
            this.connectionProvider = mysqlProvider;
            this.transactionManager = new JdbcTransactionManager(mysqlProvider);

            this.roomRepository = new JdbcRoomRepository(mysqlProvider);
            this.bookingRepository = new JdbcBookingRepository(mysqlProvider);
            this.customerRepository = new JdbcCustomerRepository(mysqlProvider);
            this.serviceRepository = new JdbcServiceRepository(mysqlProvider);
            this.serviceUsageRepository = new JdbcServiceUsageRepository(mysqlProvider);
            this.invoiceRepository = new JdbcInvoiceRepository(mysqlProvider);
        } else {
            this.connectionProvider = null;
            this.transactionManager = new NoOpTransactionManager();

            this.roomRepository = new InMemoryRoomRepository();
            this.bookingRepository = new InMemoryBookingRepository();
            this.customerRepository = new InMemoryCustomerRepository();
            this.serviceRepository = new InMemoryServiceRepository();
            this.serviceUsageRepository = new InMemoryServiceUsageRepository();
            this.invoiceRepository = new InMemoryInvoiceRepository();
        }

        this.listRoomsUseCase = new ListRoomsUseCase(roomRepository);
        this.getRoomDetailUseCase = new GetRoomDetailUseCase(roomRepository);
        this.updateRoomStatusUseCase = new UpdateRoomStatusUseCase(roomRepository);
        this.createBookingUseCase = new CreateBookingUseCase(bookingRepository, customerRepository, roomRepository);
        this.listAvailableServicesUseCase = new ListAvailableServicesUseCase(serviceRepository);
        this.addServiceToBookingUseCase = new AddServiceToBookingUseCase(serviceRepository, serviceUsageRepository, bookingRepository);
        this.servicesListActiveBookingsUseCase = new com.hotel.services.application.ListActiveBookingsUseCase(bookingRepository);
        this.checkoutListActiveBookingsUseCase = new com.hotel.checkout.application.ListActiveBookingsUseCase(bookingRepository);
        this.calculateCheckoutUseCase = new CalculateCheckoutUseCase(bookingRepository, roomRepository, serviceUsageRepository, serviceRepository);
        this.checkoutUseCase = new CheckoutUseCase(calculateCheckoutUseCase, invoiceRepository, bookingRepository, roomRepository);
        this.getRevenueReportUseCase = new GetRevenueReportUseCase(invoiceRepository);
    }

    public RoomsPanel buildRoomsPanel() {
        return new RoomsPanel(listRoomsUseCase, getRoomDetailUseCase, updateRoomStatusUseCase);
    }

    public BookingPanel buildBookingPanel(Runnable onBookingCreated) {
        return new BookingPanel(createBookingUseCase, listRoomsUseCase, onBookingCreated);
    }

    public ServicesPanel buildServicesPanel() {
        return new ServicesPanel(listAvailableServicesUseCase, addServiceToBookingUseCase, servicesListActiveBookingsUseCase);
    }

    public CheckoutPanel buildCheckoutPanel(Runnable onCheckoutSuccess) {
        return new CheckoutPanel(checkoutListActiveBookingsUseCase, calculateCheckoutUseCase, checkoutUseCase, onCheckoutSuccess);
    }

    public RevenuePanel buildRevenuePanel() {
        return new RevenuePanel(getRevenueReportUseCase);
    }

    private Properties loadProperties() {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream("app.properties")) {
            properties.load(fis);
        } catch (IOException ignored) {
            // use defaults
        }
        return properties;
    }
}