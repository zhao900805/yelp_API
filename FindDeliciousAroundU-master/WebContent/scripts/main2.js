/* step1: ()() */
(function() {

  /* step2: variables */
  var user_id = '';
  //	var user_id = 'fatfat';
  var user_fullname = '';
  var lng = -122.08;
  var lat = 37.38;
  var loc='';

  /* step3: main function(entrance) */
  init();

  /* step4: define init function */
  function init() {
    // Register event listeners
//    new
    $('login-btn').addEventListener('click', login);
    $('sign-up-btn').addEventListener('click', signUp);
    $('sign-btn').addEventListener('click', showRegisterForm);
    $('sign-up-link').addEventListener('click', showRegisterForm);
    $('login-link').addEventListener('click', showLoginForm);
//    new
    $('nearby-btn').addEventListener('click', loadNearbyItems);
    $('fav-btn').addEventListener('click', loadFavoriteItems);
    $('recommendation-btn').addEventListener('click', loadRecommendedItems);

    // validateSession();
    var loginForm = $('login-form');
    var registerForm = $('register-form');
    var logoutBtn = $('logout-link');
    
    hideElement(loginForm);
    hideElement(registerForm);
    hideElement(logoutBtn);
//    for debugger
//    onSessionValid({
//      user_id:'1111',
//      name: 'John Smith'
//    });
    
    var welcomeMsg = $('welcome-msg');
    welcomeMsg.innerHTML = 'Welcome, ' + user_fullname;

    // step 7
    initGeoLocation();
  }

  /**
  * Session
  */
  function validateSession() {
    var url = './login';
    var req = JSON.stringify();
    
    showLoadingMessage('Validating Session');
    
    // make AJAX Call
    ajax('GET', url, req, 
    function(res) {
      var result = JSON.parse(res);
      
      if (result.result === 'SUCCESS') {
        onSessionValid(result);
      }
    });
  }
  
  function onSessionValid(result) {
    user_id = result.user_id;
    user_fullname = result.name;
    
    var loginForm = $('login-form');
    var registerForm = $('register-form');
    var itemNav = $('item-nav');
    var itemList = $('item-list');
    var avatar = $('avatar');
    var welcomeMsg = $('welcome-msg');
    var logoutBtn = $('logout-link');
    var signUpBtn = $('sign-up-link');
    var loginBtn = $('login-link');
    
    
    welcomeMsg.innerHTML = 'Welcome, ' + user_fullname;
    
    showElement(itemNav);
    showElement(itemList);
    showElement(avatar, 'inline-block');
    showElement(welcomeMsg, 'inline-block');
    showElement(logoutBtn, 'inline-block');
    hideElement(signUpBtn);
    hideElement(loginBtn);
    hideElement(loginForm);
    
    hideElement(registerForm);
    
//    initGeoLocation();
  }
  
  function onSessionInvalid() {
    var loginForm = $('login-form');
    var registerForm = $('register-form');
    var itemNav = $('item-nav');
    var itemList = $('item-list');
    var avatar = $('avatar');
    var welcomeMsg = $('welcome-msg');
    var logoutBtn = $('logout-link');
    var signUpBtn = $('sign-up-link');
    var loginBtn = $('login-link');
    
    hideElement(itemNav);
    hideElement(itemList);
    hideElement(avatar);
    hideElement(welcomeMsg);
    hideElement(logoutBtn);
    showElement(signUpBtn, 'inline-block');
    showElement(loginBtn, 'inline-block');
    showElement(loginForm);
    
    hideElement(registerForm);
  }
  
  function hideElement(element) {
    element.style.display = 'none';
  }
  function showElement(element, style) {
    var displayStyle = style ? style : 'block';
    element.style.display = displayStyle;
  }
  /* step5: create $ function */
  /**
	 * A helper function that creates a DOM element <tag options...>
	 */
  function $(tag, options) {
    if (!options) {
      return document.getElementById(tag);
    }
    var element = document.createElement(tag);

    for ( var option in options) {
      if (options.hasOwnProperty(option)) {
        element[option] = options[option];
      }
    }
    return element;
  }

  /* step6: create AJAX helper function */
  /**
	 * @param method - GET|POST|PUT|DELETE
	 * @param url - API end point
	 * @param callback - This the successful callback
	 * @param errorHandler - This is the failed callback
	 */
  function ajax(method, url, data, callback, errorHandler) {
    var xhr = new XMLHttpRequest();

    xhr.open(method, url, true);

    xhr.onload = function() {
      if (xhr.status === 200) {
        callback(xhr.responseText);
      } else if (xhr.status === 403) {
    	  onSessionInvalid();
      } else {
        errorHandler();
      }
    };

    xhr.onerror = function() {
      console.error("The request couldn't be completed.");
      errorHandler();
    };

    if (data === null) {
      xhr.send();
    } else {
      xhr.setRequestHeader("Content-Type",
                           "application/json;charset=utf-8");
      xhr.send(data);
    }
  }

  /** step 7: initGeoLocation function **/
  function initGeoLocation() {
    if (navigator.geolocation) {
      // step 8
      navigator.geolocation.getCurrentPosition(onPositionUpdated,
                                               onLoadPositionFailed, {
        maximumAge : 60000
      });
      showLoadingMessage('Retrieving your location...');
    } else {
      // step 9
      onLoadPositionFailed();
    }
  }

  /** step 8: onPositionUpdated function **/
  function onPositionUpdated(position) {
    lat = position.coords.latitude;
    lng = position.coords.longitude;

    // step 11
    loadNearbyItems();
  }

  /** step 9: onPositionUpdated function **/
  function onLoadPositionFailed() {
    console.warn('navigator.geolocation is not available');

    //step 10
    getLocationFromIP();
  }

  /** step 10: getLocationFromIP function **/
  function getLocationFromIP() {
    // Get location from http://ipinfo.io/json
    var url = 'http://ipinfo.io/json'
    var req = null;
    ajax('GET', url, req, function(res) {
      var result = JSON.parse(res);
      if ('loc' in result) {
        var loc = result.loc.split(',');
        lat = loc[0];
        lng = loc[1];
      } else {
        console.warn('Getting location by IP failed.');
      }
      // step 11
      loadNearbyItems();
    });
  }

  /** step 11: loadNearbyItems function **/
  /**
	 * API #1 Load the nearby items API end point: [GET]
	 * /Dashi/search?user_id=1111&lat=37.38&lon=-122.08
	 */
  function loadNearbyItems() {
    console.log('loadNearbyItems');
    // step 12
    activeBtn('nearby-btn');

    // The request parameters
    //		var url = './search';
    var url = './restaurant';
    var params = 'user_id=' + user_id +'&loc=' + loc + '&lat=' + lat + '&lon=' + lng;
    //		var params = 'loc=11551OhioAve,LosAngeles,CA';
    var req = JSON.stringify({});

    // step 13
    // display loading message
    showLoadingMessage('Loading nearby items...');

    // make AJAX call
    ajax('GET', url + '?' + params, req,
         // successful callback
         function(res) {
      var items = JSON.parse(res);
      if (!items || items.length === 0) {
        // step 14
        showWarningMessage('No nearby item.');
      } else {
        // step 16
        listItems(items);
      }
    },
         // failed callback
         function() {
      // step 15
      showErrorMessage('Cannot load nearby items.');
    });
  }

  /** step 12: activeBtn function **/

  /**
	 * A helper function that makes a navigation button active
	 * 
	 * @param btnId - The id of the navigation button
	 */
  function activeBtn(btnId) {
    var btns = document.getElementsByClassName('main-nav-btn');

    // deactivate all navigation buttons
    for (var i = 0; i < btns.length; i++) {
      btns[i].className = btns[i].className.replace(/\bactive\b/, '');
    }

    // active the one that has id = btnId
    var btn = $(btnId);
    btn.className += ' active';
  }

  /** step 13: showLoadingMessage function **/
  function showLoadingMessage(msg) {
    var itemList = $('item-list');
    itemList.innerHTML = '<p class="notice"><i class="fa fa-spinner fa-spin"></i> '
      + msg + '</p>';
  }

  /** step 14: showWarningMessage function **/
  function showWarningMessage(msg) {
    var itemList = $('item-list');
    itemList.innerHTML = '<p class="notice"><i class="fa fa-exclamation-triangle"></i> '
      + msg + '</p>';
  }

  /** step15: showErrorMessage function **/
  function showErrorMessage(msg) {
    var itemList = $('item-list');
    itemList.innerHTML = '<p class="notice"><i class="fa fa-exclamation-circle"></i> '
      + msg + '</p>';
  }

  /** step16: listItems function **/
  /**
	 * @param items - An array of item JSON objects
	 */
  function listItems(items) {
    // Clear the current results
    var itemList = $('item-list');
    itemList.innerHTML = '';

    for (var i = 0; i < items.length; i++) {
      // step 17
      console.log("lalalallal");
      addItem(itemList, items[i]);

    }
  }

  /** step17: addItem function **/
  /**
	 * Add item to the list
	 * @param itemList - The <ul id="item-list"> tag
	 * @param item - The item data (JSON object)
	 */
  function addItem(itemList, item) {
    var item_id = item.item_id;

    // create the <li> tag and specify the id and class attributes
    var li = $('li', {
      id : 'item-' + item_id,
      className : 'item'
    });

    // set the data attribute
    li.dataset.item_id = item_id;
    li.dataset.favorite = item.favorite;

    // item image
    if (item.image_url) {
      li.appendChild($('img', {
        src : item.image_url
      }));
    } else {
      li.appendChild($('img', {
        src : 'https://assets-cdn.github.com/images/modules/logos_page/GitHub-Mark.png'
      }))
    }
    // section
    var section = $('div', {});

    // title
    var title = $('a', {
      href : item.url,
      target : '_blank',
      className : 'item-name'
    });
    title.innerHTML = item.name;
    section.appendChild(title);

    // category
    var category = $('p', {
      className : 'item-category'
    });
    category.innerHTML = 'Category: ' + item.categories.join(', ');
    section.appendChild(category);

    var stars = $('div', {
      className : 'stars'
    });

    for (var i = 0; i < item.raring; i++) {
      var star = $('i', {
        className : 'fa fa-star'
      });
      stars.appendChild(star);
    }

    if (('' + item.rating).match(/\.5$/)) {
      stars.appendChild($('i', {
        className : 'fa fa-star-half-o'
      }));
    }

    section.appendChild(stars);

    li.appendChild(section);

    // address
    var address = $('p', {
      className : 'item-address'
    });

    address.innerHTML = item.location.replace(/,/g, '<br/>').replace(/\"/g,
                                                                     '');
    li.appendChild(address);

    // favorite link
    var favLink = $('p', {
      className : 'fav-link'
    });

    favLink.onclick = function() {
      // step 20:
      changeFavoriteItem(item_id);
    };


    favLink.appendChild($('i', {
      id : 'fav-icon-' + item_id,
      className : item.favorite ? 'fa fa-heart' : 'fa fa-heart-o'
    }));

    li.appendChild(favLink);

    itemList.appendChild(li);
  }

  // step 18:

  function loadFavoriteItems() {
    activeBtn('fav-btn');

    // The request parameters
    var url = './history';
    var params = 'user_id=' + user_id;
    var req = JSON.stringify({});

    // display loading message
    showLoadingMessage('Loading favorite items...');

    // make AJAX call
    ajax('GET', url + '?' + params, req, function(res) {
      console.log(res);
      var items = JSON.parse(res);
      if (!items || items.length === 0) {
        showWarningMessage('No favorite item.');
      } else {
        listItems(items);
      }
    }, function() {
      showErrorMessage('Cannot load favorite items.');
    });
  }



  // step 19:
  function loadRecommendedItems() {
    console.log("load recommended items...");

    activeBtn("recommendation-btn");

    var url = "./recommendation";
    var params = "user_id=" + user_id + "&lat=" + lat + "&lng" + lng;
    var req = JSON.stringify({});

    showLoadingMessage("Loading recommended items...");

    // make AJAX call
    ajax("GET", url + "?" + params, req, function(res) {
      var items = JSON.parse(res);
      if (!items || items.length === 0) {
        showWarningMessage("No Favorite item.");
      } else {
        listItems(items);
      }
    }, function() {
      showErrorMessage("Cannot load recommended items.");
    });
  }

  // step 20: set favorite items
  function changeFavoriteItem(item_id) {
    // Check whether this item has been visited or not
    var li = $('item-' + item_id);
    var favIcon = $('fav-icon-' + item_id);

    var favorite = li.dataset.favorite !== 'true';

    // The request parameters
    var url = './history';
    var req = JSON.stringify({
      user_id: user_id,
      favorite: [item_id]
    });
    var method = favorite ? 'POST' : 'DELETE';

    ajax(method, url, req,
         // successful callback
         function(res) {
      var result = JSON.parse(res);
      if (result.result === 'SUCCESS') {
        li.dataset.favorite = favorite;
        favIcon.className = favorite ? 'fa fa-heart' : 'fa fa-heart-o';
      } else {
        favIcon.innerHTML = 'lalala';
      }
    },
         // failed callback
         function() {
      showErrorMessage('Cannot set favorite items.');
    });
  }
  
  // log in function
  function login() {
    var username = $('username').value;
    var password = $('password').value;
    
    //password = md5(username + md5(password));
    
    var url = './login';
    var req = JSON.stringify({
      user_id : username,
      password : password,
    });
    
    ajax('POST', url, req,
    function(res) {
      var result = JSON.parse(res);
      
      if (result.result === 'SUCCESS') {
        onSessionValid(result);
      }
    },
    function() {
      showLoginError();
    });
  }
  
  function showLoginError() {
    $('login-error').innerHTML = 'Invalid username or password';
  }
  
  function clearLoginError() {
    $('login-error').innerHTML = '';
  }
  
  // sign up function
  // sign up function
  function signUp() {
    var loginForm = $('login-form');
    var registerForm = $('register-form');
    var itemNav = $('item-nav');
    var itemList = $('item-list');
    var avatar = $('avatar');
    var welcomeMsg = $('welcome-msg');
    var logoutBtn = $('logout-link');
    var signUpBtn = $('sign-up-link');

    hideElement(itemNav);
    hideElement(itemList);
    hideElement(avatar);
    hideElement(welcomeMsg);
    hideElement(logoutBtn);
    showElement(signUpBtn, 'inline-block');
    hideElement(loginForm);
  
    showElement(registerForm);
  
  
    var username = $('username-signUp').value;
    var password = $('password-signUp').value;
    var firstname = $('firstname').value;
    var lastname = $('lastname').value;

    // password = md5(username + md5(password));

    var url = './register';
    var req = JSON.stringify({
      user_id : username,
      password : password,
      first_name : firstname,
      last_name : lastname,
    });

    ajax('POST', url, req,
         function(res) {
      var result = JSON.parse(res);

      if (result.result === 'SUCCESS') {
        onSessionValid(result);
      }
    },
         function() {
    	showRegisterError();
    });
  }
  
  function showRegisterError() {
	  $('register-error').innerHTML = 'The username has been token.';
  }
  
  function showRegisterForm() {
	    var loginForm = $('login-form');
	    var registerForm = $('register-form');
	    var itemNav = $('item-nav');
	    var itemList = $('item-list');
	    var avatar = $('avatar');
	    var welcomeMsg = $('welcome-msg');
	    var logoutBtn = $('logout-link');
	    var signUpBtn = $('sign-up-link');

	    hideElement(itemNav);
	    hideElement(itemList);
	    hideElement(avatar);
	    hideElement(welcomeMsg);
	    hideElement(logoutBtn);
	    showElement(signUpBtn, 'inline-block');
	    hideElement(loginForm);

	    showElement(registerForm);
  }
  
  function showLoginForm() {
		var loginForm = $('login-form');
		var registerForm = $('register-form');
		var itemNav = $('item-nav');
		var itemList = $('item-list');
		var avatar = $('avatar');
		var welcomeMsg = $('welcome-msg');
		var logoutBtn = $('logout-link');
		var signUpBtn = $('sign-up-link');

		hideElement(itemNav);
		hideElement(itemList);
		hideElement(avatar);
		hideElement(welcomeMsg);
		hideElement(logoutBtn);
		showElement(signUpBtn, 'inline-block');
		showElement(loginForm);

		hideElement(registerForm);
  }
})();