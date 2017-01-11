/**
 * jQuery的Dialog插件。
 *
 * @param object content
 * @param object options 选项。
 * @return 
 */
var _version = '1.0';
var _zindex = 500;
var	_count = 1;
function Dialog(content, options) {
	
	var defaults = {
		// 标题文本，若不想显示title请通过CSS设置其display为none 
		title : 'Title',
		// 是否显示标题栏。
		showTitle : true,
		// 关闭按钮文字，若不想显示关闭按钮请通过CSS设置其display为none 
		closeText : '[Close]',
		// 是否移动 
		draggable : true,
		// 是否是模态对话框 
		modal : true,
		// 是否居中。 
		center : true,
		// 是否跟随页面滚动。
		fixed : true,
		// 自动关闭时间，为0表示不会自动关闭。 
		time : 0,
		// 对话框的id，若为false，则由系统自动产生。
		id : false,
		// 宽度，如为定义则默认100%
		width : '100%',
		// 高度，如为定义则默认100%
		height : '100%',
		timeOut : 0
	};
	var options = $.extend(defaults, options);
	// 唯一ID
	options.id = options.id ? options.id : 'dialog-' + _count;
	// 遮罩层ID
	var overlayId = options.id + '-overlay';
	// 自动关闭计时器 
	var timeId = null;
	var isShow = false;
	var isIe = $.browser.msie;
	var isIe6 = $.browser.msie && ('6.0' == $.browser.version);

	var c_height = options.height.split("px")[0] - 27;
	/* 对话框的布局及标题内容。*/
	var barHtml = !options.showTitle ? ''
			: '<div class="bar"><span class="title">' + options.title
					+ '</span><a class="close">' + options.closeText
					+ '</a></div>';
	var dialog = $(
			'<div id="' + options.id + '" class="dialog">' + barHtml + '<div class="content" style="height:'+ c_height +'px"></div></div>').hide();
	$('body').append(dialog);

	/**
	 * 重置对话框的位置。
	 *
	 * 主要是在需要居中的时候，每次加载完内容，都要重新定位
	 *
	 * @return void
	 */
	var resetPos = function() {
		/* 是否需要居中定位，必需在已经知道了dialog元素大小的情况下，才能正确居中，也就是要先设置dialog的内容。 */
		if (options.center) {
			var left = ($(window).width() - dialog.width()) / 2;
			var top = ($(window).height() - dialog.height()) / 2;
			if (!isIe6 && options.fixed) {
				dialog.css({
					top : top,
					left : left
				});
			} else {
				dialog.css({
					top : top + $(document).scrollTop(),
					left : left + $(document).scrollLeft()
				});
			}
		}
	}

	/**
	 * 初始化位置及一些事件函数。
	 *
	 * 其中的this表示Dialog对象而不是init函数。
	 */
	var init = function() {
		/* 是否需要初始化背景遮罩层 */
		if (options.modal) {
			$('body')
					.append(
							'<div id="' + overlayId
									+ '" class="dialog-overlay"></div>');
			$('#' + overlayId).css({
				'left' : 0,
				'top' : 0,
				'width':$(document).width(),
				'height':$(document).height(),
				'z-index' : ++_zindex,
				'position' : 'absolute'
			}).hide();
		}
		dialog.css({
			'z-index' : ++_zindex,
			'position' : options.fixed ? 'fixed' : 'absolute',
			'width' : options.width,
			'height' : options.height
		});
		/*  IE6 兼容fixed代码 */
		if (isIe6 && options.fixed) {
			dialog.css('position', 'absolute');
			resetPos();
			var top = parseInt(dialog.css('top')) - $(document).scrollTop();
			var left = parseInt(dialog.css('left')) - $(document).scrollLeft();
			$(window).scroll(function() {
				dialog.css({
					'top' : $(document).scrollTop() + top,
					'left' : $(document).scrollLeft() + left
				});
			});
		}
		/* 以下代码处理框体是否可以移动 */
		var mouse = {
			x : 0,
			y : 0
		};
		function moveDialog(event) {
			var e = window.event || event;
			var top = parseInt(dialog.css('top')) + (e.clientY - mouse.y);
			var left = parseInt(dialog.css('left')) + (e.clientX - mouse.x);
			dialog.css({
				top : top,
				left : left
			});
			mouse.x = e.clientX;
			mouse.y = e.clientY;
		}
		;
		dialog.find('.bar').mousedown(function(event) {
			if (!options.draggable) {
				return;
			}

			var e = window.event || event;
			mouse.x = e.clientX;
			mouse.y = e.clientY;
			$(document).bind('mousemove', moveDialog);
		});
		$(document).mouseup(function(event) {
			$(document).unbind('mousemove', moveDialog);
		});

		/* 绑定一些相关事件。 */
		dialog.find('.close').bind('click', this.close);
		dialog.bind('mousedown', function() {
			dialog.css('z-index', ++_zindex);
		});

		// 自动关闭 
		if (0 != options.time) {
			timeId = setTimeout(this.close, options.time);
		}
	}

	/**
	 * 设置对话框的内容。 
	 *
	 * @param string c 可以是HTML文本。
	 * @return void
	 */
	this.setContent = function(c) {
		var div = dialog.find('.content');
		if ('object' == typeof (c)) {
			switch (c.type.toLowerCase()) {
			case 'id':
				// 将ID的内容复制过来，原来的还在。
				div.html($('#' + c.value).html());
				break;
			case 'img':
				div.html('loading...');
				$('<img alt="" />').load(function() {
					div.empty().append($(this));
					resetPos();
				}).attr('src', c.value);
				break;
			case 'url':
				div.html('loading...');
				setTimeout(function(){
					$.ajax({
						url : c.value,
						success : function(html) {
							div.html(html);
							resetPos();
						},
						error : function(xml, textStatus, error) {
							div.html('error...')
						}
					});
				},options.timeOut)
				break;
			case 'iframe':
				div.append($('<iframe src="' + c.value + '"/>'));
				break;
			case 'text':
			default:
				div.html(c.value);
				break;
			}
		} else {
			div.html(c);
		}
	}

	/**
	 * 显示对话框
	 */
	this.show = function() {
		if (undefined != options.beforeShow && !options.beforeShow()) {
			return;
		}
		/**
		 * 获得某一元素的透明度。IE从滤境中获得。
		 *
		 * @return float
		 */
		var getOpacity = function(id) {
			if (!isIe) {
				return $('#' + id).css('opacity');
			}
			var el = document.getElementById(id);
			return (undefined != el && undefined != el.filters
					&& undefined != el.filters.alpha && undefined != el.filters.alpha.opacity) ? el.filters.alpha.opacity / 100
					: 1;
		}
		/* 是否显示背景遮罩层 */
		if (options.modal) {
			$('#' + overlayId).fadeTo('slow', getOpacity(overlayId));
		}
		dialog.fadeTo('slow', getOpacity(options.id), function() {
			if (undefined != options.afterShow) {
				options.afterShow();
			}
			isShow = true;
		});
		// 自动关闭 
		if (0 != options.time) {
			timeId = setTimeout(this.close, options.time);
		}
		resetPos();
	}

	/**
	 * 隐藏对话框。但并不取消窗口内容。
	 */
	this.hide = function() {
		if (!isShow) {
			return;
		}
		if (undefined != options.beforeHide && !options.beforeHide()) {
			return;
		}
		dialog.fadeOut('slow', function() {
			if (undefined != options.afterHide) {
				options.afterHide();
			}
		});
		if (options.modal) {
			$('#' + overlayId).fadeOut('slow');
		}
		isShow = false;
	}

	/**
	 * 关闭对话框 
	 *
	 * @return void
	 */
	this.close = function() {
		if (undefined != options.beforeClose && !options.beforeClose()) {
			return;
		}
		dialog.fadeOut('slow', function() {
			$(this).remove();
			isShow = false;
			if (undefined != options.afterClose) {
				options.afterClose();
			}
		});
		if (options.modal) {
			$('#' + overlayId).fadeOut('slow', function() {
				$(this).remove();
			});
		}
		clearTimeout(timeId);
	}
	init.call(this);
	this.setContent(content);
	_count++;
	_zindex++;
}