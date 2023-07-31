package com.kapusniak.tomasz.service;

import com.kapusniak.tomasz.entity.CustomerEntity;
import com.kapusniak.tomasz.entity.OrderEntity;
import com.kapusniak.tomasz.mapper.CustomerEntityMapper;
import com.kapusniak.tomasz.openapi.model.Customer;
import com.kapusniak.tomasz.repository.PageSize;
import com.kapusniak.tomasz.repository.jpa.CustomerJpaRepository;
import com.kapusniak.tomasz.service.model.CustomerService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class CustomerServiceTest {

    private static final UUID CUSTOMER_UUID_1 = UUID.fromString("29755321-c483-4a12-9f64-30a132038b70");
    private static final Integer PAGE_NUMBER = 0;
    private static final PageRequest PAGEABLE = PageRequest.of(PAGE_NUMBER, PageSize.EXTRA_SMALL.getValue());

    @Mock
    private CustomerJpaRepository customerRepository;

    @Mock
    private CustomerEntityMapper customerEntityMapper;
    @InjectMocks
    private CustomerService customerService;

    private Customer prepareCustomer() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setEmail("test@email.com");
        customer.setFirstName("testFirstName");
        customer.setLastName("testLastName");
        customer.setUuid(CUSTOMER_UUID_1);

        return customer;
    }

    private CustomerEntity prepareCustomerEntity() {
        CustomerEntity customerEntity = new CustomerEntity();
        customerEntity.setId(1L);
        customerEntity.setEmail("test@email.com");
        customerEntity.setFirstName("testFirstName");
        customerEntity.setLastName("testLastName");
        customerEntity.setUuid(CUSTOMER_UUID_1);


        return customerEntity;
    }

    private Page<CustomerEntity> prepareCustomerEntityList() {
        List<CustomerEntity> customerEntities = List.of(prepareCustomerEntity(), prepareCustomerEntity());

        return new PageImpl<>(customerEntities);

    }

    @BeforeEach
    void setup() {
        when(customerEntityMapper
                .mapToEntity(any(Customer.class)))
                .thenReturn(prepareCustomerEntity());
        when(customerEntityMapper
                .mapToApiModel(any(CustomerEntity.class)))
                .thenReturn(prepareCustomer());
    }

    @Test
    @DisplayName("should correctly save an Customer entity exactly once")
    void save() {

        // given
        CustomerEntity customerEntity = prepareCustomerEntity();
        Customer customer = prepareCustomer();

        // and
        when(customerRepository
                .save(any(CustomerEntity.class)))
                .thenReturn(customerEntity);

        // when
        Customer result = customerService.save(customer);

        // then
        then(customerRepository)
                .should(times(1))
                .save(customerEntityMapper.mapToEntity(customer));

        // verify
        assertThat(customer).isEqualTo(result);
    }

    @Test
    @DisplayName("should throw an exception when Customer entity is null and check method invocations")
    void saveNull() {

        // given
        Customer customer = null;

        // when
        Throwable throwable = catchThrowable(() ->
                customerService.save(customer));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Saving customer failed. Customer is null.");

        // verify
        then(customerRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("should return page of customer with correct size and check method invocations")
    void findAll() {

        // given
        Page<CustomerEntity> customerEntityPage = prepareCustomerEntityList();

        // and
        given(customerRepository.findAll(any(Pageable.class)))
                .willReturn(customerEntityPage);

        // when
        Page<Customer> customerPage = customerService.findAll(PAGE_NUMBER);

        // then
        assertThat(customerPage.getContent().size())
                .isEqualTo(2);

        // verify
        then(customerRepository)
                .should(times(1))
                .findAll(PAGEABLE);
    }

    @Test
    @DisplayName("should return customer based on customer uuid")
    void findByUuid() {

        // given
        CustomerEntity customerEntity = prepareCustomerEntity();
        UUID customerUuid = CUSTOMER_UUID_1;

        // and
        given(customerRepository.findByUuid(
                any(UUID.class)))
                .willReturn(Optional.of(customerEntity));

        // when
        Customer customerByUuid = customerService.findByUuid(customerUuid);

        // then
        assertThat(customerByUuid.getUuid())
                .isNotNull();
    }

    @Test
    @DisplayName("should throw an exception when customer uuid is null")
    void findByUuidNull() {

        // given
        UUID customerUuid = null;

        // when
        Throwable throwable = catchThrowable(() ->
                customerService.findByUuid(customerUuid));

        // then
        assertThat(throwable)
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Searching for customer failed. Customer uuid is null.");

        // verify
        then(customerRepository)
                .shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("should delete an customer based on customer uuid")
    void delete() {

        // given
        CustomerEntity customerEntity = new CustomerEntity();
        Customer customer = new Customer();
        UUID customerUuid = CUSTOMER_UUID_1;

        // and
        given(customerRepository.findByUuid(
                any(UUID.class)))
                .willReturn(Optional.of(customerEntity));

        // when
        customerService.delete(customerUuid);

        // then
        then(customerRepository)
                .should(times(1))
                .delete(customerEntityMapper.mapToEntity(customer));
    }

    @Test
    @DisplayName("should throw an exception when customer uuid is null")
    void deleteNull() {
        // given
        UUID customerUuid = null;

        // when
        Throwable throwable = catchThrowable(() ->
                customerService.delete(customerUuid));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Deleting customer failed. Customer uuid is null.");
    }

    @Test
    @DisplayName("should throw an exception when uuid is null")
    void updateNullUuid() {
        // given
        Customer customer = new Customer();
        UUID customerUuid = null;

        // when
        Throwable throwable = catchThrowable(() ->
                customerService.update(customerUuid, customer));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Updating customer failed. Customer uuid is null.");
    }

    @Test
    @DisplayName("should throw an exception when customer is null")
    void updateNullCustomer() {
        // given
        UUID customerUuid = CUSTOMER_UUID_1;
        Customer customer = null;

        // when
        Throwable thrown = catchThrowable(() ->
                customerService.update(customerUuid, customer));

        // then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Updating customer failed. Customer is null.");
    }

    @Test
    @DisplayName("should throw an exception when newCustomer's uuid doesn't match customerFromDb's uuid")
    void updateUuidMissMatch() {
        // given
        CustomerEntity customerEntity = new CustomerEntity();
        UUID oldUuid = CUSTOMER_UUID_1;

        Customer newCustomer = new Customer();
        UUID newUuid = UUID.randomUUID();
        newCustomer.setUuid(newUuid);

        // and
        when(customerRepository.findByUuid(any(UUID.class)))
                .thenReturn(Optional.of(customerEntity));
        // when
        Throwable throwable = catchThrowable(() ->
                customerService.update(oldUuid, newCustomer));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Updating customer fields failed. Different uuid's");
    }

    @Test
    @DisplayName("should correctly update customer when valid uuid and customer are provided")
    void shouldUpdateCustomer() {
        // given
        CustomerEntity customerEntity = prepareCustomerEntity();
        UUID customerUuid = CUSTOMER_UUID_1;

        Customer changedCustomer = prepareCustomerToEdit();
        CustomerEntity changedCustomerEntity = prepareCustomerEntityToEdit();

        // and
        when(customerRepository.findByUuid(any(UUID.class)))
                .thenReturn(Optional.of(customerEntity));
        when(customerRepository.save(any(CustomerEntity.class)))
                .thenReturn(changedCustomerEntity);

        //and
        when(customerEntityMapper
                .mapToApiModel(changedCustomerEntity))
                .thenReturn(changedCustomer);

        // when
        Customer updatedCustomer = customerService.update(customerUuid, changedCustomer);

        // then
        assertThat(updatedCustomer).isNotNull();
        assertThat(updatedCustomer.getUuid()).isEqualTo(changedCustomer.getUuid());
        assertThat(updatedCustomer.getFirstName()).isEqualTo(changedCustomer.getFirstName());
        assertThat(updatedCustomer.getLastName()).isEqualTo(changedCustomer.getLastName());
        assertThat(updatedCustomer.getEmail()).isEqualTo(changedCustomer.getEmail());

        assertThat(updatedCustomer.getOrders().get(1)).isEqualTo(changedCustomer.getOrders().get(1));
        assertThat(updatedCustomer.getOrders().get(1)).isEqualTo(changedCustomer.getOrders().get(1));

        // verify
        then(customerRepository)
                .should(times(1))
                .save(customerEntity);
    }

    private Customer prepareCustomerToEdit() {
        UUID customerUuid = CUSTOMER_UUID_1;
        String newFirstName = "newFirstName";
        String newLastName = "newLastName";
        String newEmail = "newEmail";
        UUID newOrder1Uuid = UUID.randomUUID();
        UUID newOrder2Uuid = UUID.randomUUID();

        Customer changedCustomer = new Customer();
        changedCustomer.setUuid(customerUuid);
        changedCustomer.setFirstName(newFirstName);
        changedCustomer.setLastName(newLastName);
        changedCustomer.setEmail(newEmail);

        changedCustomer.setOrders(List.of(newOrder1Uuid, newOrder2Uuid));

        return changedCustomer;
    }

    private CustomerEntity prepareCustomerEntityToEdit() {
        UUID customerUuid = CUSTOMER_UUID_1;
        String newFirstName = "newFirstName";
        String newLastName = "newLastName";
        String newEmail = "newEmail";
        UUID newOrder1Uuid = UUID.randomUUID();
        UUID newOrder2Uuid = UUID.randomUUID();

        CustomerEntity changedCustomerEntity = new CustomerEntity();
        changedCustomerEntity.setUuid(customerUuid);
        changedCustomerEntity.setFirstName(newFirstName);
        changedCustomerEntity.setLastName(newLastName);
        changedCustomerEntity.setEmail(newEmail);
        OrderEntity newOrderEntity1 = new OrderEntity();
        newOrderEntity1.setUuid(newOrder1Uuid);
        OrderEntity newOrderEntity2 = new OrderEntity();
        newOrderEntity2.setUuid(newOrder2Uuid);

        changedCustomerEntity.setOrders(List.of(newOrderEntity1, newOrderEntity2));

        return changedCustomerEntity;
    }
}