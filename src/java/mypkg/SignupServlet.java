package mypkg;
 
import java.io.*;
import java.util.*;
import java.util.logging.*;
import javax.naming.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import javax.sql.*;
 
public class SignupServlet extends HttpServlet {
 
   private DataSource pool;  // Database connection pool
 
   @Override
   public void init(ServletConfig config) throws ServletException {
      try {
         // Create a JNDI Initial context to be able to lookup the DataSource
         InitialContext ctx = new InitialContext();
         // Lookup the DataSource
         pool = (DataSource)ctx.lookup("jdbc/mysql_ebookshop");
         if (pool == null)
            throw new ServletException("Unknown DataSource 'jdbc/mysql_ebookshop'");
      } catch (NamingException ex) {
         Logger.getLogger(LoginServlet.class.getName()).log(Level.SEVERE, null, ex);
      }
   }
 
   @Override
   protected void doGet(HttpServletRequest request, HttpServletResponse response)
           throws ServletException, IOException {
      response.setContentType("text/html;charset=UTF-8");
      PrintWriter out = response.getWriter();
 
      Connection conn = null;
      Statement  stmt = null;
      try {
         out.println("<html><head><title>Signup</title></head><body>");
         out.println("<h2>Signup</h2>");
 
         conn = pool.getConnection();  // Get a connection from the pool
         stmt = conn.createStatement();
 
         // Retrieve and process request parameters: username and password
         String userName = request.getParameter("username");
         String password = request.getParameter("password");
         String role = request.getParameter("role");
         boolean hasUserName = userName != null && ((userName = userName.trim()).length() > 0);
         boolean hasPassword = password != null && ((password = password.trim()).length() > 0);
 
         // Validate input request parameters
         if (!hasUserName)
         {
            out.println("<h3>Please Enter Your username!</h3>");
         }
         else if (!hasPassword)
         {
            out.println("<h3>Please Enter Your password!</h3>");
         }
         else
         {
            StringBuilder sqlStr = new StringBuilder();
            sqlStr.append("SELECT role FROM users, user_roles WHERE ");
            sqlStr.append("STRCMP(users.username, '");
            sqlStr.append(userName).append("') = 0 ");
            sqlStr.append("AND STRCMP(users.password, PASSWORD('");
            sqlStr.append(password).append("')) = 0 ");
            sqlStr.append("AND users.username = user_roles.username");
            
            
            ResultSet rset = stmt.executeQuery(sqlStr.toString());
 
            // Check if username/password are correct
            if (!rset.next()) 
            {  // User Not Found Create a new User!!
               ResultSet resultset = null;
               String sqlString = null;
               sqlString = "INSERT INTO users values ('"
                    + userName + "', password('" + password + "'))";
               
            //System.out.println(sqlStr);  // for debugging
            stmt.executeUpdate(sqlString);
            sqlString = "INSERT INTO user_roles values ('" + userName + "', '" + role + "')";   
            stmt.executeUpdate(sqlString);
            out.println(" You Have Successfully Signed Up !!");
            out.println("<h1>Hello, " + userName + "!</h1>");
            out.println("<h2>Welcome to the Book Store</h2>");
            out.println("<p><a href='start'>Search for Books</a></p>");
            }
            else
            {
                out.println("User Already Exists");  
            }
         
         }
         out.println("</body></html>");
 
      } catch (SQLException ex) {
          out.println(ex.toString());
         out.println("<h3>Service not available. Try again later!</h3></body></html>");
         Logger.getLogger(LoginServlet.class.getName()).log(Level.SEVERE, null, ex);
      } finally {
         out.close();
         try {
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();  // Return the connection to the pool
         } catch (SQLException ex) {
            Logger.getLogger(LoginServlet.class.getName()).log(Level.SEVERE, null, ex);
         }
      }
   }
 
   @Override
   protected void doPost(HttpServletRequest request, HttpServletResponse response)
           throws ServletException, IOException {
      doGet(request, response);
   }
}