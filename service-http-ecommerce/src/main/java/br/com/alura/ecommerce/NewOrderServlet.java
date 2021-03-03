package br.com.alura.ecommerce;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.servlet.Source;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class NewOrderServlet extends HttpServlet {

    private final KafkaDispatcher<Order> orderDispatcher = new KafkaDispatcher<>();
    private final KafkaDispatcher<Email> emailDispatcher = new KafkaDispatcher<>();

    @Override
    public void destroy() {
        super.destroy();
        orderDispatcher.close();
        emailDispatcher.close();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        var email = req.getParameter("email");
        var amount = new BigDecimal(req.getParameter("amount"));

        var orderId = UUID.randomUUID().toString();

        var order = new Order(orderId, amount, email);

        var emailCode = new Email("Welcome, we are processing your order!", "This is an email");

        try {
            orderDispatcher.send("ECOMMERCE_NEW_ORDER", email, order);
            emailDispatcher.send("ECOMMERCE_SEND_EMAIL", email, emailCode);

            System.out.println("New order sent successfully");

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().println("New order sent successfully");
        } catch (ExecutionException | InterruptedException e) {
            throw new ServletException(e);
        }
    }
}
