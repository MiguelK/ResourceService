<%@ page import="com.rs.Resource" %><%--
  Created by IntelliJ IDEA.
  User: miguelkrantz
  Date: 2017-05-06
  Time: 09:42
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Podcast Citat</title>

    <%

        String fileName = request.getParameter("fileName");


        if(fileName==null) {
            fileName = (String)request.getAttribute("fileName");
        }

        Resource resource = Resource.create(fileName); //request.getParameter("fileName"));

        if(resource.isNonExisting() || resource.isInvalid()){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,"No resource with fileName="
                    + request.getParameter("fileName") + " exists");

            //FIXME
        //    return;
        }

        String podCastTitle = "podCastTitle";
        String podCastEpisodeTitle = "podCastEpisodeTitle";
        String podCastImageURL = "https://friendsarena.se/media/715340/liten-karusell-evenemangssida-592x322.jpg";

       // if(fileName != null) {
            //FIXME Property
            String videoStreamURL = "http://resourceservice-itemstore.rhcloud.com/ResourceService/stream/" + fileName;
          //  response.sendRedirect(videoStreamURL);
        //}
    %>

    <!-- Apple smart banner -->

  <!--  <meta name="apple-itunes-app" content="app-id=1209200428", app-argument=http://www.pods.one"> -->


    <!-- Schema.org markup for Google+ -->
    <meta itemprop="name" content="<%=podCastTitle%>">
    <meta itemprop="description" content="<%=podCastEpisodeTitle%>">
    <meta itemprop="image" content="<%=podCastImageURL%>">

    <!-- Facebook -->
    <meta property="og:video:height" content="640" />
    <meta property="og:video:width" content="385" />
    <meta property="og:video" content="<%=videoStreamURL%>"/>
    <meta property="og:type" content="video/quicktime"/>

    <meta property="og:title" content="<%=podCastTitle%>"/>
    <meta property="og:description"
          content="<%=podCastEpisodeTitle%>"/>
    <meta property="og:image" content="<%=podCastImageURL%>"/>


    <!-- Twitter -->
    <meta name="twitter:site" content="@Pods">
    <meta name="twitter:title" content="<%=podCastTitle%>">
    <meta name="twitter:description" content="<%=podCastEpisodeTitle%>">
    <meta name="twitter:image" content="<%=podCastImageURL%>">
    <meta name="twitter:card" content="summary_large_image">

</head>

<body>

<%

    String localAddr = request.getServerName();
    int localPort = request.getLocalPort();
    String contextPath = request.getServletContext().getContextPath();

   // String fileName = (String) request.getAttribute("fileName");

    String streamUrl = "http://" + localAddr + contextPath + "/stream/" + fileName;
    if(localAddr.toLowerCase().contains("local")){
         streamUrl = "http://" + localAddr + ":" + localPort + contextPath + "/stream/" + fileName;
    }
%>

Stream2=
<%=streamUrl%>
<br>


<video controls autoplay style="width:480px;height: auto">
    <source src="<%=streamUrl%>" type="video/quicktime" />
    <p>Your browser does not support HTML5 audio.</p>
</video>

</body>
</html>
