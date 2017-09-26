<%@ page import="com.rs.Resource" %><%--
  Created by IntelliJ IDEA.
  User: miguelkrantz
  Date: 2017-05-07
  Time: 14:49
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>

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
        // response.sendRedirect(redirectURL);
        //}
    %>

</head>
<body>



<!-- Load Facebook SDK for JavaScript -->
<div id="fb-root"></div>
<script>(function(d, s, id) {
    var js, fjs = d.getElementsByTagName(s)[0];
    if (d.getElementById(id)) return;
    js = d.createElement(s); js.id = id;
    js.src = "//connect.facebook.net/en_US/sdk.js#xfbml=1&version=v2.6";
    fjs.parentNode.insertBefore(js, fjs);
}(document, 'script', 'facebook-jssdk'));</script>

<!-- Your embedded video player code -->
<div class="fb-video" data-href="<%=videoStreamURL%>" data-width="500" data-show-text="false">
    <div class="fb-xfbml-parse-ignore">
        <blockquote cite="https://www.facebook.com/facebook/videos/10153231379946729/">
            <a href="https://www.facebook.com/facebook/videos/10153231379946729/">How to Share With Just Friends</a>
            <p>How to share with just friends.</p>
            Posted by <a href="https://www.facebook.com/facebook/">Facebook</a> on Friday, December 5, 2014
        </blockquote>
    </div>
</div>
</body>
</html>
