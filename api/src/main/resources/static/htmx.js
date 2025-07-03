var htmx = (function() {
  'use strict'

  // Public API
  const htmx = {
    // Tsc madness here, assigning the functions directly results in an invalid TypeScript output, but reassigning is fine
    /* Event processing */
    /** @type {typeof onLoadHelper} */
    onLoad: null,
    /** @type {typeof processNode} */
    process: null,
    /** @type {typeof addEventListenerImpl} */
    on: null,
    /** @type {typeof removeEventListenerImpl} */
    off: null,
    /** @type {typeof triggerEvent} */
    trigger: null,
    /** @type {typeof ajaxHelper} */
    ajax: null,
    /* DOM querying helpers */
    /** @type {typeof find} */
    find: null,
    /** @type {typeof findAll} */
    findAll: null,
    /** @type {typeof closest} */
    closest: null,
    /**
     * Returns the input values that would resolve for a given element via the htmx value resolution mechanism
     *
     * @see https://htmx.org/api/#values
     *
     * @param {Element} elt the element to resolve values on
     * @param {HttpVerb} type the request type (e.g. **get** or **post**) non-GET's will include the enclosing form of the element. Defaults to **post**
     * @returns {Object}
     */
    values: function(elt, type) {
      const inputValues = getInputValues(elt, type || 'post')
      return inputValues.values
    },
    /* DOM manipulation helpers */
    /** @type {typeof removeElement} */
    remove: null,
    /** @type {typeof addClassToElement} */
    addClass: null,
    /** @type {typeof removeClassFromElement} */
    removeClass: null,
    /** @type {typeof toggleClassOnElement} */
    toggleClass: null,
    /** @type {typeof takeClassForElement} */
    takeClass: null,
    /** @type {typeof swap} */
    swap: null,
    /* Extension entrypoints */
    /** @type {typeof defineExtension} */
    defineExtension: null,
    /** @type {typeof removeExtension} */
    removeExtension: null,
    /* Debugging */
    /** @type {typeof logAll} */
    logAll: null,
    /** @type {typeof logNone} */
    logNone: null,
    /* Debugging */
    /**
     * The logger htmx uses to log with
     *
     * @see https://htmx.org/api/#logger
     */
    logger: null,
    /**
     * A property holding the configuration htmx uses at runtime.
     *
     * Note that using a [meta tag](https://htmx.org/docs/#config) is the preferred mechanism for setting these properties.
     *
     * @see https://htmx.org/api/#config
     */
    config: {
      /**
       * Whether to use history.
       * @type boolean
       * @default true
       */
      historyEnabled: true,
      /**
       * The number of pages to keep in **localStorage** for history support.
       * @type number
       * @default 10
       */
      historyCacheSize: 10,
      /**
       * @type boolean
       * @default false
       */
      refreshOnHistoryMiss: false,
      /**
       * The default swap style to use if **[hx-swap](https://htmx.org/attributes/hx-swap)** is omitted.
       * @type HtmxSwapStyle
       * @default 'innerHTML'
       */
      defaultSwapStyle: 'innerHTML',
      /**
       * The default delay between receiving a response from the server and doing the swap.
       * @type number
       * @default 0
       */
      defaultSwapDelay: 0,
      /**
       * The default delay between completing the content swap and settling attributes.
       * @type number
       * @default 20
       */
      defaultSettleDelay: 20,
      /**
       * If true, htmx will inject a small amount of CSS into the page to make indicators invisible unless the **htmx-indicator** class is present.
       * @type boolean
       * @default true
       */
      includeIndicatorStyles: true,
      /**
       * The class to place on indicators when a request is in flight.
       * @type string
       * @default 'htmx-indicator'
       */
      indicatorClass: 'htmx-indicator',
      /**
       * The class to place on triggering elements when a request is in flight.
       * @type string
       * @default 'htmx-request'
       */
      requestClass: 'htmx-request',
      /**
       * The class to temporarily place on elements that htmx has added to the DOM.
       * @type string
       * @default 'htmx-added'
       */
      addedClass: 'htmx-added',
      /**
       * The class to place on target elements when htmx is in the settling phase.
       * @type string
       * @default 'htmx-settling'
       */
      settlingClass: 'htmx-settling',
      /**
       * The class to place on target elements when htmx is in the swapping phase.
       * @type string
       * @default 'htmx-swapping'
       */
      swappingClass: 'htmx-swapping',
      /**
       * Allows the use of eval-like functionality in htmx, to enable **hx-vars**, trigger conditions & script tag evaluation. Can be set to **false** for CSP compatibility.
       * @type boolean
       * @default true
       */
      allowEval: true,
      /**
       * If set to false, disables the interpretation of script tags.
       * @type boolean
       * @default true
       */
      allowScriptTags: true,
      /**
       * If set, the nonce will be added to inline scripts.
       * @type string
       * @default ''
       */
      inlineScriptNonce: '',
      /**
       * If set, the nonce will be added to inline styles.
       * @type string
       * @default ''
       */
      inlineStyleNonce: '',
      /**
       * The attributes to settle during the settling phase.
       * @type string[]
       * @default ['class', 'style', 'width', 'height']
       */
      attributesToSettle: ['class', 'style', 'width', 'height'],
      /**
       * Allow cross-site Access-Control requests using credentials such as cookies, authorization headers or TLS client certificates.
       * @type boolean
       * @default false
       */
      withCredentials: false,
      /**
       * @type number
       * @default 0
       */
      timeout: 0,
      /**
       * The default implementation of **getWebSocketReconnectDelay** for reconnecting after unexpected connection loss by the event code **Abnormal Closure**, **Service Restart** or **Try Again Later**.
       * @type {'full-jitter' | ((retryCount:number) => number)}
       * @default "full-jitter"
       */
      wsReconnectDelay: 'full-jitter',
      /**
       * The type of binary data being received over the WebSocket connection
       * @type BinaryType
       * @default 'blob'
       */
      wsBinaryType: 'blob',
      /**
       * @type string
       * @default '[hx-disable], [data-hx-disable]'
       */
      disableSelector: '[hx-disable], [data-hx-disable]',
      /**
       * @type {'auto' | 'instant' | 'smooth'}
       * @default 'instant'
       */
      scrollBehavior: 'instant',
      /**
       * If the focused element should be scrolled into view.
       * @type boolean
       * @default false
       */
      defaultFocusScroll: false,
      /**
       * If set to true htmx will include a cache-busting parameter in GET requests to avoid caching partial responses by the browser
       * @type boolean
       * @default false
       */
      getCacheBusterParam: false,
      /**
       * If set to true, htmx will use the View Transition API when swapping in new content.
       * @type boolean
       * @default false
       */
      globalViewTransitions: false,
      /**
       * htmx will format requests with these methods by encoding their parameters in the URL, not the request body
       * @type {(HttpVerb)[]}
       * @default ['get', 'delete']
       */
      methodsThatUseUrlParams: ['get', 'delete'],
      /**
       * If set to true, disables htmx-based requests to non-origin hosts.
       * @type boolean
       * @default false
       */
      selfRequestsOnly: true,
      /**
       * If set to true htmx will not update the title of the document when a title tag is found in new content
       * @type boolean
       * @default false
       */
      ignoreTitle: false,
      /**
       * Whether the target of a boosted element is scrolled into the viewport.
       * @type boolean
       * @default true
       */
      scrollIntoViewOnBoost: true,
      /**
       * The cache to store evaluated trigger specifications into.
       * You may define a simple object to use a never-clearing cache, or implement your own system using a [proxy object](https://developer.mozilla.org/docs/Web/JavaScript/Reference/Global_Objects/Proxy)
       * @type {Object|null}
       * @default null
       */
      triggerSpecsCache: null,
      /** @type boolean */
      disableInheritance: false,
      /** @type HtmxResponseHandlingConfig[] */
      responseHandling: [
        { code: '204', swap: false },
        { code: '[23]..', swap: true },
        { code: '[45]..', swap: false, error: true }
      ],
      /**
       * Whether to process OOB swaps on elements that are nested within the main response element.
       * @type boolean
       * @default true
       */
      allowNestedOobSwaps: true
    },
    /** @type {typeof parseInterval} */
    parseInterval: null,
    /** @type {typeof internalEval} */
    _: null,
    version: '2.0.2'
  }
  // Tsc madness part 2
  htmx.onLoad = onLoadHelper
  htmx.process = processNode
  htmx.on = addEventListenerImpl
  htmx.off = removeEventListenerImpl
  htmx.trigger = triggerEvent
  htmx.ajax = ajaxHelper
  htmx.find = find
  htmx.findAll = findAll
  htmx.closest = closest
  htmx.remove = removeElement
  htmx.addClass = addClassToElement
  htmx.removeClass = removeClassFromElement
  htmx.toggleClass = toggleClassOnElement
  htmx.takeClass = takeClassForElement
  htmx.swap = swap
  htmx.defineExtension = defineExtension
  htmx.removeExtension = removeExtension
  htmx.logAll = logAll
  htmx.logNone = logNone
  htmx.parseInterval = parseInterval
  htmx._ = internalEval

  const internalAPI = {
    addTriggerHandler,
    bodyContains,
    canAccessLocalStorage,
    findThisElement,
    filterValues,
    swap,
    hasAttribute,
    getAttributeValue,
    getClosestAttributeValue,
    getClosestMatch,
    getExpressionVars,
    getHeaders,
    getInputValues,
    getInternalData,
    getSwapSpecification,
    getTriggerSpecs,
    getTarget,
    makeFragment,
    mergeObjects,
    makeSettleInfo,
    oobSwap,
    querySelectorExt,
    settleImmediately,
    shouldCancel,
    triggerEvent,
    triggerErrorEvent,
    withExtensions
  }

  const VERBS = ['get', 'post', 'put', 'delete', 'patch']
  const VERB_SELECTOR = VERBS.map(function(verb) {
    return '[hx-' + verb + '], [data-hx-' + verb + ']'
  }).join(', ')

  const HEAD_TAG_REGEX = makeTagRegEx('head')

  //= ===================================================================
  // Utilities
  //= ===================================================================

  /**
   * @param {string} tag
   * @param {boolean} global
   * @returns {RegExp}
   */
  function makeTagRegEx(tag, global = false) {
    return new RegExp(`<${tag}(\\s[^>]*>|>)([\\s\\S]*?)<\\/${tag}>`,
      global ? 'gim' : 'im')
  }

  /**
   * Parses an interval string consistent with the way htmx does. Useful for plugins that have timing-related attributes.
   *
   * Caution: Accepts an int followed by either **s** or **ms**. All other values use **parseFloat**
   *
   * @see https://htmx.org/api/#parseInterval
   *
   * @param {string} str timing string
   * @returns {number|undefined}
   */
  function parseInterval(str) {
    if (str == undefined) {
      return undefined
    }

    let interval = NaN
    if (str.slice(-2) == 'ms') {
      interval = parseFloat(str.slice(0, -2))
    } else if (str.slice(-1) == 's') {
      interval = parseFloat(str.slice(0, -1)) * 1000
    } else if (str.slice(-1) == 'm') {
      interval = parseFloat(str.slice(0, -1)) * 1000 * 60
    } else {
      interval = parseFloat(str)
    }
    return isNaN(interval) ? undefined : interval
  }

  /**
   * @param {Node} elt
   * @param {string} name
   * @returns {(string | null)}
   */
  function getRawAttribute(elt, name) {
    return elt instanceof Element && elt.getAttribute(name)
  }

  /**
   * @param {Element} elt
   * @param {string} qualifiedName
   * @returns {boolean}
   */
  // resolve with both hx and data-hx prefixes
  function hasAttribute(elt, qualifiedName) {
    return !!elt.hasAttribute && (elt.hasAttribute(qualifiedName) ||
      elt.hasAttribute('data-' + qualifiedName))
  }

  /**
   *
   * @param {Node} elt
   * @param {string} qualifiedName
   * @returns {(string | null)}
   */
  function getAttributeValue(elt, qualifiedName) {
    return getRawAttribute(elt, qualifiedName) || getRawAttribute(elt, 'data-' + qualifiedName)
  }

  /**
   * @param {Node} elt
   * @returns {Node | null}
   */
  function parentElt(elt) {
    const parent = elt.parentElement
    if (!parent && elt.parentNode instanceof ShadowRoot) return elt.parentNode
    return parent
  }

  /**
   * @returns {Document}
   */
  function getDocument() {
    return document
  }

  /**
   * @param {Node} elt
   * @param {boolean} global
   * @returns {Node|Document}
   */
  function getRootNode(elt, global) {
    return elt.getRootNode ? elt.getRootNode({ composed: global }) : getDocument()
  }

  /**
   * @param {Node} elt
   * @param {(e:Node) => boolean} condition
   * @returns {Node | null}
   */
  function getClosestMatch(elt, condition) {
    while (elt && !condition(elt)) {
      elt = parentElt(elt)
    }

    return elt || null
  }

  /**
   * @param {Element} initialElement
   * @param {Element} ancestor
   * @param {string} attributeName
   * @returns {string|null}
   */
  function getAttributeValueWithDisinheritance(initialElement, ancestor, attributeName) {
    const attributeValue = getAttributeValue(ancestor, attributeName)
    const disinherit = getAttributeValue(ancestor, 'hx-disinherit')
    var inherit = getAttributeValue(ancestor, 'hx-inherit')
    if (initialElement !== ancestor) {
      if (htmx.config.disableInheritance) {
        if (inherit && (inherit === '*' || inherit.split(' ').indexOf(attributeName) >= 0)) {
          return attributeValue
        } else {
          return null
        }
      }
      if (disinherit && (disinherit === '*' || disinherit.split(' ').indexOf(attributeName) >= 0)) {
        return 'unset'
      }
    }
    return attributeValue
  }

  /**
   * @param {Element} elt
   * @param {string} attributeName
   * @returns {string | null}
   */
  function getClosestAttributeValue(elt, attributeName) {
    let closestAttr = null
    getClosestMatch(elt, function(e) {
      return !!(closestAttr = getAttributeValueWithDisinheritance(elt, asElement(e), attributeName))
    })
    if (closestAttr !== 'unset') {
      return closestAttr
    }
  }

  /**
   * @param {Node} elt
   * @param {string} selector
   * @returns {boolean}
   */
  function matches(elt, selector) {
    // @ts-ignore: non-standard properties for browser compatibility
    // noinspection JSUnresolvedVariable
    const matchesFunction = elt instanceof Element && (elt.matches || elt.matchesSelector || elt.msMatchesSelector || elt.mozMatchesSelector || elt.webkitMatchesSelector || elt.oMatchesSelector)
    return !!matchesFunction && matchesFunction.call(elt, selector)
  }

  /**
   * @param {string} str
   * @returns {string}
   */
  function getStartTag(str) {
    const tagMatcher = /<([a-z][^\/\0>\x20\t\r\n\f]*)/i
    const match = tagMatcher.exec(str)
    if (match) {
      return match[1].toLowerCase()
    } else {
      return ''
    }
  }

  /**
   * @param {string} resp
   * @returns {Document}
   */
  function parseHTML(resp) {
    const parser = new DOMParser()
    return parser.parseFromString(resp, 'text/html')
  }

  /**
   * @param {DocumentFragment} fragment
   * @param {Node} elt
   */
  function takeChildrenFor(fragment, elt) {
    while (elt.childNodes.length > 0) {
      fragment.append(elt.childNodes[0])
    }
  }

  /**
   * @param {HTMLScriptElement} script
   * @returns {HTMLScriptElement}
   */
  function duplicateScript(script) {
    const newScript = getDocument().createElement('script')
    forEach(script.attributes, function(attr) {
      newScript.setAttribute(attr.name, attr.value)
    })
    newScript.textContent = script.textContent
    newScript.async = false
    if (htmx.config.inlineScriptNonce) {
      newScript.nonce = htmx.config.inlineScriptNonce
    }
    return newScript
  }

  /**
   * @param {HTMLScriptElement} script
   * @returns {boolean}
   */
  function isJavaScriptScriptNode(script) {
    return script.matches('script') && (script.type === 'text/javascript' || script.type === 'module' || script.type === '')
  }

  /**
   * we have to make new copies of script tags that we are going to insert because
   * SOME browsers (not saying who, but it involves an element and an animal) don't
   * execute scripts created in <template> tags when they are inserted into the DOM
   * and all the others do lmao
   * @param {DocumentFragment} fragment
   */
  function normalizeScriptTags(fragment) {
    Array.from(fragment.querySelectorAll('script')).forEach(/** @param {HTMLScriptElement} script */ (script) => {
      if (isJavaScriptScriptNode(script)) {
        const newScript = duplicateScript(script)
        const parent = script.parentNode
        try {
          parent.insertBefore(newScript, script)
        } catch (e) {
          logError(e)
        } finally {
          script.remove()
        }
      }
    })
  }

  /**
   * @typedef {DocumentFragment & {title?: string}} DocumentFragmentWithTitle
   * @description  a document fragment representing the response HTML, including
   * a `title` property for any title information found
   */

  /**
   * @param {string} response HTML
   * @returns {DocumentFragmentWithTitle}
   */
  function makeFragment(response) {
    // strip head tag to determine shape of response we are dealing with
    const responseWithNoHead = response.replace(HEAD_TAG_REGEX, '')
    const startTag = getStartTag(responseWithNoHead)
    /** @type DocumentFragmentWithTitle */
    let fragment
    if (startTag === 'html') {
      // if it is a full document, parse it and return the body
      fragment = /** @type DocumentFragmentWithTitle */ (new DocumentFragment())
      const doc = parseHTML(response)
      takeChildrenFor(fragment, doc.body)
      fragment.title = doc.title
    } else if (startTag === 'body') {
      // parse body w/o wrapping in template
      fragment = /** @type DocumentFragmentWithTitle */ (new DocumentFragment())
      const doc = parseHTML(responseWithNoHead)
      takeChildrenFor(fragment, doc.body)
      fragment.title = doc.title
    } else {
      // otherwise we have non-body partial HTML content, so wrap it in a template to maximize parsing flexibility
      const doc = parseHTML('<body><template class="internal-htmx-wrapper">' + responseWithNoHead + '</template></body>')
      fragment = /** @type DocumentFragmentWithTitle */ (doc.querySelector('template').content)
      // extract title into fragment for later processing
      fragment.title = doc.title

      // for legacy reasons we support a title tag at the root level of non-body responses, so we need to handle it
      var titleElement = fragment.querySelector('title')
      if (titleElement && titleElement.parentNode === fragment) {
        titleElement.remove()
        fragment.title = titleElement.innerText
      }
    }
    if (fragment) {
      if (htmx.config.allowScriptTags) {
        normalizeScriptTags(fragment)
      } else {
        // remove all script tags if scripts are disabled
        fragment.querySelectorAll('script').forEach((script) => script.remove())
      }
    }
    return fragment
  }

  /**
   * @param {Function} func
   */
  function maybeCall(func) {
    if (func) {
      func()
    }
  }

  /**
   * @param {any} o
   * @param {string} type
   * @returns
   */
  function isType(o, type) {
    return Object.prototype.toString.call(o) === '[object ' + type + ']'
  }

  /**
   * @param {*} o
   * @returns {o is Function}
   */
  function isFunction(o) {
    return typeof o === 'function'
  }

  /**
   * @param {*} o
   * @returns {o is Object}
   */
  function isRawObject(o) {
    return isType(o, 'Object')
  }

  /**
   * @typedef {Object} OnHandler
   * @property {(keyof HTMLElementEventMap)|string} event
   * @property {EventListener} listener
   */

  /**
   * @typedef {Object} ListenerInfo
   * @property {string} trigger
   * @property {EventListener} listener
   * @property {EventTarget} on
   */

  /**
   * @typedef {Object} HtmxNodeInternalData
   * Element data
   * @property {number} [initHash]
   * @property {boolean} [boosted]
   * @property {OnHandler[]} [onHandlers]
   * @property {number} [timeout]
   * @property {ListenerInfo[]} [listenerInfos]
   * @property {boolean} [cancelled]
   * @property {boolean} [triggeredOnce]
   * @property {number} [delayed]
   * @property {number|null} [throttle]
   * @property {string} [lastValue]
   * @property {boolean} [loaded]
   * @property {string} [path]
   * @property {string} [verb]
   * @property {boolean} [polling]
   * @property {HTMLButtonElement|HTMLInputElement|null} [lastButtonClicked]
   * @property {number} [requestCount]
   * @property {XMLHttpRequest} [xhr]
   * @property {(() => void)[]} [queuedRequests]
   * @property {boolean} [abortable]
   *
   * Event data
   * @property {HtmxTriggerSpecification} [triggerSpec]
   * @property {EventTarget[]} [handledFor]
   */

  /**
   * getInternalData retrieves "private" data stored by htmx within an element
   * @param {EventTarget|Event} elt
   * @returns {HtmxNodeInternalData}
   */
  function getInternalData(elt) {
    const dataProp = 'htmx-internal-data'
    let data = elt[dataProp]
    if (!data) {
      data = elt[dataProp] = {}
    }
    return data
  }

  /**
   * toArray converts an ArrayLike object into a real array.
   * @template T
   * @param {ArrayLike<T>} arr
   * @returns {T[]}
   */
  function toArray(arr) {
    const returnArr = []
    if (arr) {
      for (let i = 0; i < arr.length; i++) {
        returnArr.push(arr[i])
      }
    }
    return returnArr
  }

  /**
   * @template T
   * @param {T[]|NamedNodeMap|HTMLCollection|HTMLFormControlsCollection|ArrayLike<T>} arr
   * @param {(T) => void} func
   */
  function forEach(arr, func) {
    if (arr) {
      for (let i = 0; i < arr.length; i++) {
        func(arr[i])
      }
    }
  }

  /**
   * @param {Element} el
   * @returns {boolean}
   */
  function isScrolledIntoView(el) {
    const rect = el.getBoundingClientRect()
    const elemTop = rect.top
    const elemBottom = rect.bottom
    return elemTop < window.innerHeight && elemBottom >= 0
  }

  /**
   * @param {Node} elt
   * @returns {boolean}
   */
  function bodyContains(elt) {
    // IE Fix
    const rootNode = elt.getRootNode && elt.getRootNode()
    if (rootNode && rootNode instanceof window.ShadowRoot) {
      return getDocument().body.contains(rootNode.host)
    } else {
      return getDocument().body.contains(elt)
    }
  }

  /**
   * @param {string} trigger
   * @returns {string[]}
   */
  function splitOnWhitespace(trigger) {
    return trigger.trim().split(/\s+/)
  }

  /**
   * mergeObjects takes all the keys from
   * obj2 and duplicates them into obj1
   * @template T1
   * @template T2
   * @param {T1} obj1
   * @param {T2} obj2
   * @returns {T1 & T2}
   */
  function mergeObjects(obj1, obj2) {
    for (const key in obj2) {
      if (obj2.hasOwnProperty(key)) {
        // @ts-ignore tsc doesn't seem to properly handle types merging
        obj1[key] = obj2[key]
      }
    }
    // @ts-ignore tsc doesn't seem to properly handle types merging
    return obj1
  }

  /**
   * @param {string} jString
   * @returns {any|null}
   */
  function parseJSON(jString) {
    try {
      return JSON.parse(jString)
    } catch (error) {
      logError(error)
      return null
    }
  }

  /**
   * @returns {boolean}
   */
  function canAccessLocalStorage() {
    const test = 'htmx:localStorageTest'
    try {
      localStorage.setItem(test, test)
      localStorage.removeItem(test)
      return true
    } catch (e) {
      return false
    }
  }

  /**
   * @param {string} path
   * @returns {string}
   */
  function normalizePath(path) {
    try {
      const url = new URL(path)
      if (url) {
        path = url.pathname + url.search
      }
      // remove trailing slash, unless index page
      if (!(/^\/$/.test(path))) {
        path = path.replace(/\/+$/, '')
      }
      return path
    } catch (e) {
      // be kind to IE11, which doesn't support URL()
      return path
    }
  }

  //= =========================================================================================
  // public API
  //= =========================================================================================

  /**
   * @param {string} str
   * @returns {any}
   */
  function internalEval(str) {
    return maybeEval(getDocument().body, function() {
      return eval(str)
    })
  }

  /**
   * Adds a callback for the **htmx:load** event. This can be used to process new content, for example initializing the content with a javascript library
   *
   * @see https://htmx.org/api/#onLoad
   *
   * @param {(elt: Node) => void} callback the callback to call on newly loaded content
   * @returns {EventListener}
   */
  function onLoadHelper(callback) {
    const value = htmx.on('htmx:load', /** @param {CustomEvent} evt */ function(evt) {
      callback(evt.detail.elt)
    })
    return value
  }

  /**
   * Log all htmx events, useful for debugging.
   *
   * @see https://htmx.org/api/#logAll
   */
  function logAll() {
    htmx.logger = function(elt, event, data) {
      if (console) {
        console.log(event, elt, data)
      }
    }
  }

  function logNone() {
    htmx.logger = null
  }

  /**
   * Finds an element matching the selector
   *
   * @see https://htmx.org/api/#find
   *
   * @param {ParentNode|string} eltOrSelector  the root element to find the matching element in, inclusive | the selector to match
   * @param {string} [selector] the selector to match
   * @returns {Element|null}
   */
  function find(eltOrSelector, selector) {
    if (typeof eltOrSelector !== 'string') {
      return eltOrSelector.querySelector(selector)
    } else {
      return find(getDocument(), eltOrSelector)
    }
  }

  /**
   * Finds all elements matching the selector
   *
   * @see https://htmx.org/api/#findAll
   *
   * @param {ParentNode|string} eltOrSelector the root element to find the matching elements in, inclusive | the selector to match
   * @param {string} [selector] the selector to match
   * @returns {NodeListOf<Element>}
   */
  function findAll(eltOrSelector, selector) {
    if (typeof eltOrSelector !== 'string') {
      return eltOrSelector.querySelectorAll(selector)
    } else {
      return findAll(getDocument(), eltOrSelector)
    }
  }

  /**
   * @returns Window
   */
  function getWindow() {
    return window
  }

  /**
   * Removes an element from the DOM
   *
   * @see https://htmx.org/api/#remove
   *
   * @param {Node} elt
   * @param {number} [delay]
   */
  function removeElement(elt, delay) {
    elt = resolveTarget(elt)
    if (delay) {
      getWindow().setTimeout(function() {
        removeElement(elt)
        elt = null
      }, delay)
    } else {
      parentElt(elt).removeChild(elt)
    }
  }

  /**
   * @param {any} elt
   * @return {Element|null}
   */
  function asElement(elt) {
    return elt instanceof Element ? elt : null
  }

  /**
   * @param {any} elt
   * @return {HTMLElement|null}
   */
  function asHtmlElement(elt) {
    return elt instanceof HTMLElement ? elt : null
  }

  /**
   * @param {any} value
   * @return {string|null}
   */
  function asString(value) {
    return typeof value === 'string' ? value : null
  }

  /**
   * @param {EventTarget} elt
   * @return {ParentNode|null}
   */
  function asParentNode(elt) {
    return elt instanceof Element || elt instanceof Document || elt instanceof DocumentFragment ? elt : null
  }

  /**
   * This method adds a class to the given element.
   *
   * @see https://htmx.org/api/#addClass
   *
   * @param {Element|string} elt the element to add the class to
   * @param {string} clazz the class to add
   * @param {number} [delay] the delay (in milliseconds) before class is added
   */
  function addClassToElement(elt, clazz, delay) {
    elt = asElement(resolveTarget(elt))
    if (!elt) {
      return
    }
    if (delay) {
      getWindow().setTimeout(function() {
        addClassToElement(elt, clazz)
        elt = null
      }, delay)
    } else {
      elt.classList && elt.classList.add(clazz)
    }
  }

  /**
   * Removes a class from the given element
   *
   * @see https://htmx.org/api/#removeClass
   *
   * @param {Node|string} node element to remove the class from
   * @param {string} clazz the class to remove
   * @param {number} [delay] the delay (in milliseconds before class is removed)
   */
  function removeClassFromElement(node, clazz, delay) {
    let elt = asElement(resolveTarget(node))
    if (!elt) {
      return
    }
    if (delay) {
      getWindow().setTimeout(function() {
        removeClassFromElement(elt, clazz)
        elt = null
      }, delay)
    } else {
      if (elt.classList) {
        elt.classList.remove(clazz)
        // if there are no classes left, remove the class attribute
        if (elt.classList.length === 0) {
          elt.removeAttribute('class')
        }
      }
    }
  }

  /**
   * Toggles the given class on an element
   *
   * @see https://htmx.org/api/#toggleClass
   *
   * @param {Element|string} elt the element to toggle the class on
   * @param {string} clazz the class to toggle
   */
  function toggleClassOnElement(elt, clazz) {
    elt = resolveTarget(elt)
    elt.classList.toggle(clazz)
  }

  /**
   * Takes the given class from its siblings, so that among its siblings, only the given element will have the class.
   *
   * @see https://htmx.org/api/#takeClass
   *
   * @param {Node|string} elt the element that will take the class
   * @param {string} clazz the class to take
   */
  function takeClassForElement(elt, clazz) {
    elt = resolveTarget(elt)
    forEach(elt.parentElement.children, function(child) {
      removeClassFromElement(child, clazz)
    })
    addClassToElement(asElement(elt), clazz)
  }

  /**
   * Finds the closest matching element in the given elements parentage, inclusive of the element
   *
   * @see https://htmx.org/api/#closest
   *
   * @param {Element|string} elt the element to find the selector from
   * @param {string} selector the selector to find
   * @returns {Element|null}
   */
  function closest(elt, selector) {
    elt = asElement(resolveTarget(elt))
    if (elt && elt.closest) {
      return elt.closest(selector)
    } else {
      // TODO remove when IE goes away
      do {
        if (elt == null || matches(elt, selector)) {
          return elt
        }
      }
      while (elt = elt && asElement(parentElt(elt)))
      return null
    }
  }

  /**
   * @param {string} str
   * @param {string} prefix
   * @returns {boolean}
   */
  function startsWith(str, prefix) {
    return str.substring(0, prefix.length) === prefix
  }

  /**
   * @param {string} str
   * @param {string} suffix
   * @returns {boolean}
   */
  function endsWith(str, suffix) {
    return str.substring(str.length - suffix.length) === suffix
  }

  /**
   * @param {string} selector
   * @returns {string}
   */
  function normalizeSelector(selector) {
    const trimmedSelector = selector.trim()
    if (startsWith(trimmedSelector, '<') && endsWith(trimmedSelector, '/>')) {
      return trimmedSelector.substring(1, trimmedSelector.length - 2)
    } else {
      return trimmedSelector
    }
  }

  /**
   * @param {Node|Element|Document|string} elt
   * @param {string} selector
   * @param {boolean=} global
   * @returns {(Node|Window)[]}
   */
  function querySelectorAllExt(elt, selector, global) {
    elt = resolveTarget(elt)
    if (selector.indexOf('closest ') === 0) {
      return [closest(asElement(elt), normalizeSelector(selector.substr(8)))]
    } else if (selector.indexOf('find ') === 0) {
      return [find(asParentNode(elt), normalizeSelector(selector.substr(5)))]
    } else if (selector === 'next') {
      return [asElement(elt).nextElementSibling]
    } else if (selector.indexOf('next ') === 0) {
      return [scanForwardQuery(elt, normalizeSelector(selector.substr(5)), !!global)]
    } else if (selector === 'previous') {
      return [asElement(elt).previousElementSibling]
    } else if (selector.indexOf('previous ') === 0) {
      return [scanBackwardsQuery(elt, normalizeSelector(selector.substr(9)), !!global)]
    } else if (selector === 'document') {
      return [document]
    } else if (selector === 'window') {
      return [window]
    } else if (selector === 'body') {
      return [document.body]
    } else if (selector === 'root') {
      return [getRootNode(elt, !!global)]
    } else if (selector.indexOf('global ') === 0) {
      return querySelectorAllExt(elt, selector.slice(7), true)
    } else {
      return toArray(asParentNode(getRootNode(elt, !!global)).querySelectorAll(normalizeSelector(selector)))
    }
  }

  /**
   * @param {Node} start
   * @param {string} match
   * @param {boolean} global
   * @returns {Element}
   */
  var scanForwardQuery = function(start, match, global) {
    const results = asParentNode(getRootNode(start, global)).querySelectorAll(match)
    for (let i = 0; i < results.length; i++) {
      const elt = results[i]
      if (elt.compareDocumentPosition(start) === Node.DOCUMENT_POSITION_PRECEDING) {
        return elt
      }
    }
  }

  /**
   * @param {Node} start
   * @param {string} match
   * @param {boolean} global
   * @returns {Element}
   */
  var scanBackwardsQuery = function(start, match, global) {
    const results = asParentNode(getRootNode(start, global)).querySelectorAll(match)
    for (let i = results.length - 1; i >= 0; i--) {
      const elt = results[i]
      if (elt.compareDocumentPosition(start) === Node.DOCUMENT_POSITION_FOLLOWING) {
        return elt
      }
    }
  }

  /**
   * @param {Node|string} eltOrSelector
   * @param {string=} selector
   * @returns {Node|Window}
   */
  function querySelectorExt(eltOrSelector, selector) {
    if (typeof eltOrSelector !== 'string') {
      return querySelectorAllExt(eltOrSelector, selector)[0]
    } else {
      return querySelectorAllExt(getDocument().body, eltOrSelector)[0]
    }
  }

  /**
   * @template {EventTarget} T
   * @param {T|string} eltOrSelector
   * @param {T} [context]
   * @returns {Element|T|null}
   */
  function resolveTarget(eltOrSelector, context) {
    if (typeof eltOrSelector === 'string') {
      return find(asParentNode(context) || document, eltOrSelector)
    } else {
      return eltOrSelector
    }
  }

  /**
   * @typedef {keyof HTMLElementEventMap|string} AnyEventName
   */

  /**
   * @typedef {Object} EventArgs
   * @property {EventTarget} target
   * @property {AnyEventName} event
   * @property {EventListener} listener
   */

  /**
   * @param {EventTarget|AnyEventName} arg1
   * @param {AnyEventName|EventListener} arg2
   * @param {EventListener} [arg3]
   * @returns {EventArgs}
   */
  function processEventArgs(arg1, arg2, arg3) {
    if (isFunction(arg2)) {
      return {
        target: getDocument().body,
        event: asString(arg1),
        listener: arg2
      }
    } else {
      return {
        target: resolveTarget(arg1),
        event: asString(arg2),
        listener: arg3
      }
    }
  }

  /**
   * Adds an event listener to an element
   *
   * @see https://htmx.org/api/#on
   *
   * @param {EventTarget|string} arg1 the element to add the listener to | the event name to add the listener for
   * @param {string|EventListener} arg2 the event name to add the listener for | the listener to add
   * @param {EventListener} [arg3] the listener to add
   * @returns {EventListener}
   */
  function addEventListenerImpl(arg1, arg2, arg3) {
    ready(function() {
      const eventArgs = processEventArgs(arg1, arg2, arg3)
      eventArgs.target.addEventListener(eventArgs.event, eventArgs.listener)
    })
    const b = isFunction(arg2)
    return b ? arg2 : arg3
  }

  /**
   * Removes an event listener from an element
   *
   * @see https://htmx.org/api/#off
   *
   * @param {EventTarget|string} arg1 the element to remove the listener from | the event name to remove the listener from
   * @param {string|EventListener} arg2 the event name to remove the listener from | the listener to remove
   * @param {EventListener} [arg3] the listener to remove
   * @returns {EventListener}
   */
  function removeEventListenerImpl(arg1, arg2, arg3) {
    ready(function() {
      const eventArgs = processEventArgs(arg1, arg2, arg3)
      eventArgs.target.removeEventListener(eventArgs.event, eventArgs.listener)
    })
    return isFunction(arg2) ? arg2 : arg3
  }

  //= ===================================================================
  // Node processing
  //= ===================================================================

  const DUMMY_ELT = getDocument().createElement('output') // dummy element for bad selectors
  /**
   * @param {Element} elt
   * @param {string} attrName
   * @returns {(Node|Window)[]}
   */
  function findAttributeTargets(elt, attrName) {
    const attrTarget = getClosestAttributeValue(elt, attrName)
    if (attrTarget) {
      if (attrTarget === 'this') {
        return [findThisElement(elt, attrName)]
      } else {
        const result = querySelectorAllExt(elt, attrTarget)
        if (result.length === 0) {
          logError('The selector "' + attrTarget + '" on ' + attrName + ' returned no matches!')
          return [DUMMY_ELT]
        } else {
          return result
        }
      }
    }
  }

  /**
   * @param {Element} elt
   * @param {string} attribute
   * @returns {Element|null}
   */
  function findThisElement(elt, attribute) {
    return asElement(getClosestMatch(elt, function(elt) {
      return getAttributeValue(asElement(elt), attribute) != null
    }))
  }

  /**
   * @param {Element} elt
   * @returns {Node|Window|null}
   */
  function getTarget(elt) {
    console.log("elt:")
    console.log(elt)
    const targetStr = getClosestAttributeValue(elt, 'hx-target')
    console.log("the targ:")
    console.log(targetStr)
    if (targetStr) {
      if (targetStr === 'this') {
        return findThisElement(elt, 'hx-target')
      } else {
        return querySelectorExt(elt, targetStr)
      }
    } else {
      const data = getInternalData(elt)
      if (data.boosted) {
        return getDocument().body
      } else {
        return elt
      }
    }
  }

  /**
   * @param {string} name
   * @returns {boolean}
   */
  function shouldSettleAttribute(name) {
    const attributesToSettle = htmx.config.attributesToSettle
    for (let i = 0; i < attributesToSettle.length; i++) {
      if (name === attributesToSettle[i]) {
        return true
      }
    }
    return false
  }

  /**
   * @param {Element} mergeTo
   * @param {Element} mergeFrom
   */
  function cloneAttributes(mergeTo, mergeFrom) {
    forEach(mergeTo.attributes, function(attr) {
      if (!mergeFrom.hasAttribute(attr.name) && shouldSettleAttribute(attr.name)) {
        mergeTo.removeAttribute(attr.name)
      }
    })
    forEach(mergeFrom.attributes, function(attr) {
      if (shouldSettleAttribute(attr.name)) {
        mergeTo.setAttribute(attr.name, attr.value)
      }
    })
  }

  /**
   * @param {HtmxSwapStyle} swapStyle
   * @param {Element} target
   * @returns {boolean}
   */
  function isInlineSwap(swapStyle, target) {
    const extensions = getExtensions(target)
    for (let i = 0; i < extensions.length; i++) {
      const extension = extensions[i]
      try {
        if (extension.isInlineSwap(swapStyle)) {
          return true
        }
      } catch (e) {
        logError(e)
      }
    }
    return swapStyle === 'outerHTML'
  }

  /**
   * @param {string} oobValue
   * @param {Element} oobElement
   * @param {HtmxSettleInfo} settleInfo
   * @returns
   */
  function oobSwap(oobValue, oobElement, settleInfo) {
    let selector = '#' + getRawAttribute(oobElement, 'id')
    /** @type HtmxSwapStyle */
    let swapStyle = 'outerHTML'
    if (oobValue === 'true') {
      // do nothing
    } else if (oobValue.indexOf(':') > 0) {
      swapStyle = oobValue.substr(0, oobValue.indexOf(':'))
      selector = oobValue.substr(oobValue.indexOf(':') + 1, oobValue.length)
    } else {
      swapStyle = oobValue
    }

    const targets = getDocument().querySelectorAll(selector)
    if (targets) {
      forEach(
        targets,
        function(target) {
          let fragment
          const oobElementClone = oobElement.cloneNode(true)
          fragment = getDocument().createDocumentFragment()
          fragment.appendChild(oobElementClone)
          if (!isInlineSwap(swapStyle, target)) {
            fragment = asParentNode(oobElementClone) // if this is not an inline swap, we use the content of the node, not the node itself
          }

          const beforeSwapDetails = { shouldSwap: true, target, fragment }
          if (!triggerEvent(target, 'htmx:oobBeforeSwap', beforeSwapDetails)) return

          target = beforeSwapDetails.target // allow re-targeting
          if (beforeSwapDetails.shouldSwap) {
            swapWithStyle(swapStyle, target, target, fragment, settleInfo)
          }
          forEach(settleInfo.elts, function(elt) {
            triggerEvent(elt, 'htmx:oobAfterSwap', beforeSwapDetails)
          })
        }
      )
      oobElement.parentNode.removeChild(oobElement)
    } else {
      oobElement.parentNode.removeChild(oobElement)
      triggerErrorEvent(getDocument().body, 'htmx:oobErrorNoTarget', { content: oobElement })
    }
    return oobValue
  }

  /**
   * @param {DocumentFragment} fragment
   */
  function handlePreservedElements(fragment) {
    forEach(findAll(fragment, '[hx-preserve], [data-hx-preserve]'), function(preservedElt) {
      const id = getAttributeValue(preservedElt, 'id')
      const oldElt = getDocument().getElementById(id)
      if (oldElt != null) {
        preservedElt.parentNode.replaceChild(oldElt, preservedElt)
      }
    })
  }

  /**
   * @param {Node} parentNode
   * @param {ParentNode} fragment
   * @param {HtmxSettleInfo} settleInfo
   */
  function handleAttributes(parentNode, fragment, settleInfo) {
    forEach(fragment.querySelectorAll('[id]'), function(newNode) {
      const id = getRawAttribute(newNode, 'id')
      if (id && id.length > 0) {
        const normalizedId = id.replace("'", "\\'")
        const normalizedTag = newNode.tagName.replace(':', '\\:')
        const parentElt = asParentNode(parentNode)
        const oldNode = parentElt && parentElt.querySelector(normalizedTag + "[id='" + normalizedId + "']")
        if (oldNode && oldNode !== parentElt) {
          const newAttributes = newNode.cloneNode()
          cloneAttributes(newNode, oldNode)
          settleInfo.tasks.push(function() {
            cloneAttributes(newNode, newAttributes)
          })
        }
      }
    })
  }

  /**
   * @param {Node} child
   * @returns {HtmxSettleTask}
   */
  function makeAjaxLoadTask(child) {
    return function() {
      removeClassFromElement(child, htmx.config.addedClass)
      processNode(asElement(child))
      processFocus(asParentNode(child))
      triggerEvent(child, 'htmx:load')
    }
  }

  /**
   * @param {ParentNode} child
   */
  function processFocus(child) {
    const autofocus = '[autofocus]'
    const autoFocusedElt = asHtmlElement(matches(child, autofocus) ? child : child.querySelector(autofocus))
    if (autoFocusedElt != null) {
      autoFocusedElt.focus()
    }
  }

  /**
   * @param {Node} parentNode
   * @param {Node} insertBefore
   * @param {ParentNode} fragment
   * @param {HtmxSettleInfo} settleInfo
   */
  function insertNodesBefore(parentNode, insertBefore, fragment, settleInfo) {
    handleAttributes(parentNode, fragment, settleInfo)
    while (fragment.childNodes.length > 0) {
      const child = fragment.firstChild
      addClassToElement(asElement(child), htmx.config.addedClass)
      parentNode.insertBefore(child, insertBefore)
      if (child.nodeType !== Node.TEXT_NODE && child.nodeType !== Node.COMMENT_NODE) {
        settleInfo.tasks.push(makeAjaxLoadTask(child))
      }
    }
  }

  /**
   * based on https://gist.github.com/hyamamoto/fd435505d29ebfa3d9716fd2be8d42f0,
   * derived from Java's string hashcode implementation
   * @param {string} string
   * @param {number} hash
   * @returns {number}
   */
  function stringHash(string, hash) {
    let char = 0
    while (char < string.length) {
      hash = (hash << 5) - hash + string.charCodeAt(char++) | 0 // bitwise or ensures we have a 32-bit int
    }
    return hash
  }

  /**
   * @param {Element} elt
   * @returns {number}
   */
  function attributeHash(elt) {
    let hash = 0
    // IE fix
    if (elt.attributes) {
      for (let i = 0; i < elt.attributes.length; i++) {
        const attribute = elt.attributes[i]
        if (attribute.value) { // only include attributes w/ actual values (empty is same as non-existent)
          hash = stringHash(attribute.name, hash)
          hash = stringHash(attribute.value, hash)
        }
      }
    }
    return hash
  }

  /**
   * @param {EventTarget} elt
   */
  function deInitOnHandlers(elt) {
    const internalData = getInternalData(elt)
    if (internalData.onHandlers) {
      for (let i = 0; i < internalData.onHandlers.length; i++) {
        const handlerInfo = internalData.onHandlers[i]
        removeEventListenerImpl(elt, handlerInfo.event, handlerInfo.listener)
      }
      delete internalData.onHandlers
    }
  }

  /**
   * @param {Node} element
   */
  function deInitNode(element) {
    const internalData = getInternalData(element)
    if (internalData.timeout) {
      clearTimeout(internalData.timeout)
    }
    if (internalData.listenerInfos) {
      forEach(internalData.listenerInfos, function(info) {
        if (info.on) {
          removeEventListenerImpl(info.on, info.trigger, info.listener)
        }
      })
    }
    deInitOnHandlers(element)
    forEach(Object.keys(internalData), function(key) { delete internalData[key] })
  }

  /**
   * @param {Node} element
   */
  function cleanUpElement(element) {
    triggerEvent(element, 'htmx:beforeCleanupElement')
    deInitNode(element)
    // @ts-ignore IE11 code
    // noinspection JSUnresolvedReference
    if (element.children) { // IE
      // @ts-ignore
      forEach(element.children, function(child) { cleanUpElement(child) })
    }
  }

  /**
   * @param {Node} target
   * @param {ParentNode} fragment
   * @param {HtmxSettleInfo} settleInfo
   */
  function swapOuterHTML(target, fragment, settleInfo) {
    if (target instanceof Element && target.tagName === 'BODY') { // special case the body to innerHTML because DocumentFragments can't contain a body elt unfortunately
      return swapInnerHTML(target, fragment, settleInfo)
    }
    /** @type {Node} */
    let newElt
    const eltBeforeNewContent = target.previousSibling
    insertNodesBefore(parentElt(target), target, fragment, settleInfo)
    if (eltBeforeNewContent == null) {
      newElt = parentElt(target).firstChild
    } else {
      newElt = eltBeforeNewContent.nextSibling
    }
    settleInfo.elts = settleInfo.elts.filter(function(e) { return e !== target })
    // scan through all newly added content and add all elements to the settle info so we trigger
    // events properly on them
    while (newElt && newElt !== target) {
      if (newElt instanceof Element) {
        settleInfo.elts.push(newElt)
      }
      newElt = newElt.nextSibling
    }
    cleanUpElement(target)
    if (target instanceof Element) {
      target.remove()
    } else {
      target.parentNode.removeChild(target)
    }
  }

  /**
   * @param {Node} target
   * @param {ParentNode} fragment
   * @param {HtmxSettleInfo} settleInfo
   */
  function swapAfterBegin(target, fragment, settleInfo) {
    return insertNodesBefore(target, target.firstChild, fragment, settleInfo)
  }

  /**
   * @param {Node} target
   * @param {ParentNode} fragment
   * @param {HtmxSettleInfo} settleInfo
   */
  function swapBeforeBegin(target, fragment, settleInfo) {
    return insertNodesBefore(parentElt(target), target, fragment, settleInfo)
  }

  /**
   * @param {Node} target
   * @param {ParentNode} fragment
   * @param {HtmxSettleInfo} settleInfo
   */
  function swapBeforeEnd(target, fragment, settleInfo) {
    return insertNodesBefore(target, null, fragment, settleInfo)
  }

  /**
   * @param {Node} target
   * @param {ParentNode} fragment
   * @param {HtmxSettleInfo} settleInfo
   */
  function swapAfterEnd(target, fragment, settleInfo) {
    return insertNodesBefore(parentElt(target), target.nextSibling, fragment, settleInfo)
  }

  /**
   * @param {Node} target
   */
  function swapDelete(target) {
    cleanUpElement(target)
    return parentElt(target).removeChild(target)
  }

  /**
   * @param {Node} target
   * @param {ParentNode} fragment
   * @param {HtmxSettleInfo} settleInfo
   */
  function swapInnerHTML(target, fragment, settleInfo) {
    const firstChild = target.firstChild
    insertNodesBefore(target, firstChild, fragment, settleInfo)
    if (firstChild) {
      while (firstChild.nextSibling) {
        cleanUpElement(firstChild.nextSibling)
        target.removeChild(firstChild.nextSibling)
      }
      cleanUpElement(firstChild)
      target.removeChild(firstChild)
    }
  }

  /**
   * @param {HtmxSwapStyle} swapStyle
   * @param {Element} elt
   * @param {Node} target
   * @param {ParentNode} fragment
   * @param {HtmxSettleInfo} settleInfo
   */
  function swapWithStyle(swapStyle, elt, target, fragment, settleInfo) {
    switch (swapStyle) {
      case 'none':
        return
      case 'outerHTML':
        swapOuterHTML(target, fragment, settleInfo)
        return
      case 'afterbegin':
        swapAfterBegin(target, fragment, settleInfo)
        return
      case 'beforebegin':
        swapBeforeBegin(target, fragment, settleInfo)
        return
      case 'beforeend':
        swapBeforeEnd(target, fragment, settleInfo)
        return
      case 'afterend':
        swapAfterEnd(target, fragment, settleInfo)
        return
      case 'delete':
        swapDelete(target)
        return
      default:
        var extensions = getExtensions(elt)
        for (let i = 0; i < extensions.length; i++) {
          const ext = extensions[i]
          try {
            const newElements = ext.handleSwap(swapStyle, target, fragment, settleInfo)
            if (newElements) {
              if (Array.isArray(newElements)) {
                // if handleSwap returns an array (like) of elements, we handle them
                for (let j = 0; j < newElements.length; j++) {
                  const child = newElements[j]
                  if (child.nodeType !== Node.TEXT_NODE && child.nodeType !== Node.COMMENT_NODE) {
                    settleInfo.tasks.push(makeAjaxLoadTask(child))
                  }
                }
              }
              return
            }
          } catch (e) {
            logError(e)
          }
        }
        if (swapStyle === 'innerHTML') {
          swapInnerHTML(target, fragment, settleInfo)
        } else {
          swapWithStyle(htmx.config.defaultSwapStyle, elt, target, fragment, settleInfo)
        }
    }
  }

  /**
   * @param {DocumentFragment} fragment
   * @param {HtmxSettleInfo} settleInfo
   */
  function findAndSwapOobElements(fragment, settleInfo) {
    var oobElts = findAll(fragment, '[hx-swap-oob], [data-hx-swap-oob]')
    forEach(oobElts, function(oobElement) {
      if (htmx.config.allowNestedOobSwaps || oobElement.parentElement === null) {
        const oobValue = getAttributeValue(oobElement, 'hx-swap-oob')
        if (oobValue != null) {
          oobSwap(oobValue, oobElement, settleInfo)
        }
      } else {
        oobElement.removeAttribute('hx-swap-oob')
        oobElement.removeAttribute('data-hx-swap-oob')
      }
    })
    return oobElts.length > 0
  }

  /**
   * Implements complete swapping pipeline, including: focus and selection preservation,
   * title updates, scroll, OOB swapping, normal swapping and settling
   * @param {string|Element} target
   * @param {string} content
   * @param {HtmxSwapSpecification} swapSpec
   * @param {SwapOptions} [swapOptions]
   */
  function swap(target, content, swapSpec, swapOptions) {
    if (!swapOptions) {
      swapOptions = {}
    }

    target = resolveTarget(target)

    // preserve focus and selection
    const activeElt = document.activeElement
    let selectionInfo = {}
    try {
      selectionInfo = {
        elt: activeElt,
        // @ts-ignore
        start: activeElt ? activeElt.selectionStart : null,
        // @ts-ignore
        end: activeElt ? activeElt.selectionEnd : null
      }
    } catch (e) {
      // safari issue - see https://github.com/microsoft/playwright/issues/5894
    }
    const settleInfo = makeSettleInfo(target)

    // For text content swaps, don't parse the response as HTML, just insert it
    if (swapSpec.swapStyle === 'textContent') {
      target.textContent = content
    // Otherwise, make the fragment and process it
    } else {
      let fragment = makeFragment(content)

      settleInfo.title = fragment.title

      // select-oob swaps
      if (swapOptions.selectOOB) {
        const oobSelectValues = swapOptions.selectOOB.split(',')
        for (let i = 0; i < oobSelectValues.length; i++) {
          const oobSelectValue = oobSelectValues[i].split(':', 2)
          let id = oobSelectValue[0].trim()
          if (id.indexOf('#') === 0) {
            id = id.substring(1)
          }
          const oobValue = oobSelectValue[1] || 'true'
          const oobElement = fragment.querySelector('#' + id)
          if (oobElement) {
            oobSwap(oobValue, oobElement, settleInfo)
          }
        }
      }
      // oob swaps
      findAndSwapOobElements(fragment, settleInfo)
      forEach(findAll(fragment, 'template'), /** @param {HTMLTemplateElement} template */function(template) {
        if (findAndSwapOobElements(template.content, settleInfo)) {
          // Avoid polluting the DOM with empty templates that were only used to encapsulate oob swap
          template.remove()
        }
      })

      // normal swap
      if (swapOptions.select) {
        const newFragment = getDocument().createDocumentFragment()
        forEach(fragment.querySelectorAll(swapOptions.select), function(node) {
          newFragment.appendChild(node)
        })
        fragment = newFragment
      }
      handlePreservedElements(fragment)
      swapWithStyle(swapSpec.swapStyle, swapOptions.contextElement, target, fragment, settleInfo)
    }

    // apply saved focus and selection information to swapped content
    if (selectionInfo.elt &&
      !bodyContains(selectionInfo.elt) &&
      getRawAttribute(selectionInfo.elt, 'id')) {
      const newActiveElt = document.getElementById(getRawAttribute(selectionInfo.elt, 'id'))
      const focusOptions = { preventScroll: swapSpec.focusScroll !== undefined ? !swapSpec.focusScroll : !htmx.config.defaultFocusScroll }
      if (newActiveElt) {
        // @ts-ignore
        if (selectionInfo.start && newActiveElt.setSelectionRange) {
          try {
            // @ts-ignore
            newActiveElt.setSelectionRange(selectionInfo.start, selectionInfo.end)
          } catch (e) {
            // the setSelectionRange method is present on fields that don't support it, so just let this fail
          }
        }
        newActiveElt.focus(focusOptions)
      }
    }

    target.classList.remove(htmx.config.swappingClass)
    forEach(settleInfo.elts, function(elt) {
      if (elt.classList) {
        elt.classList.add(htmx.config.settlingClass)
      }
      triggerEvent(elt, 'htmx:afterSwap', swapOptions.eventInfo)
    })
    if (swapOptions.afterSwapCallback) {
      swapOptions.afterSwapCallback()
    }

    // merge in new title after swap but before settle
    if (!swapSpec.ignoreTitle) {
      handleTitle(settleInfo.title)
    }

    // settle
    const doSettle = function() {
      forEach(settleInfo.tasks, function(task) {
        task.call()
      })
      forEach(settleInfo.elts, function(elt) {
        if (elt.classList) {
          elt.classList.remove(htmx.config.settlingClass)
        }
        triggerEvent(elt, 'htmx:afterSettle', swapOptions.eventInfo)
      })

      if (swapOptions.anchor) {
        const anchorTarget = asElement(resolveTarget('#' + swapOptions.anchor))
        if (anchorTarget) {
          anchorTarget.scrollIntoView({ block: 'start', behavior: 'auto' })
        }
      }

      updateScrollState(settleInfo.elts, swapSpec)
      if (swapOptions.afterSettleCallback) {
        swapOptions.afterSettleCallback()
      }
    }

    if (swapSpec.settleDelay > 0) {
      getWindow().setTimeout(doSettle, swapSpec.settleDelay)
    } else {
      doSettle()
    }
  }

  /**
   * @param {XMLHttpRequest} xhr
   * @param {string} header
   * @param {EventTarget} elt
   */
  function handleTriggerHeader(xhr, header, elt) {
    const triggerBody = xhr.getResponseHeader(header)
    if (triggerBody.indexOf('{') === 0) {
      const triggers = parseJSON(triggerBody)
      for (const eventName in triggers) {
        if (triggers.hasOwnProperty(eventName)) {
          let detail = triggers[eventName]
          if (isRawObject(detail)) {
            // @ts-ignore
            elt = detail.target !== undefined ? detail.target : elt
          } else {
            detail = { value: detail }
          }
          triggerEvent(elt, eventName, detail)
        }
      }
    } else {
      const eventNames = triggerBody.split(',')
      for (let i = 0; i < eventNames.length; i++) {
        triggerEvent(elt, eventNames[i].trim(), [])
      }
    }
  }

  const WHITESPACE = /\s/
  const WHITESPACE_OR_COMMA = /[\s,]/
  const SYMBOL_START = /[_$a-zA-Z]/
  const SYMBOL_CONT = /[_$a-zA-Z0-9]/
  const STRINGISH_START = ['"', "'", '/']
  const NOT_WHITESPACE = /[^\s]/
  const COMBINED_SELECTOR_START = /[{(]/
  const COMBINED_SELECTOR_END = /[})]/

  /**
   * @param {string} str
   * @returns {string[]}
   */
  function tokenizeString(str) {
    /** @type string[] */
    const tokens = []
    let position = 0
    while (position < str.length) {
      if (SYMBOL_START.exec(str.charAt(position))) {
        var startPosition = position
        while (SYMBOL_CONT.exec(str.charAt(position + 1))) {
          position++
        }
        tokens.push(str.substr(startPosition, position - startPosition + 1))
      } else if (STRINGISH_START.indexOf(str.charAt(position)) !== -1) {
        const startChar = str.charAt(position)
        var startPosition = position
        position++
        while (position < str.length && str.charAt(position) !== startChar) {
          if (str.charAt(position) === '\\') {
            position++
          }
          position++
        }
        tokens.push(str.substr(startPosition, position - startPosition + 1))
      } else {
        const symbol = str.charAt(position)
        tokens.push(symbol)
      }
      position++
    }
    return tokens
  }

  /**
   * @param {string} token
   * @param {string|null} last
   * @param {string} paramName
   * @returns {boolean}
   */
  function isPossibleRelativeReference(token, last, paramName) {
    return SYMBOL_START.exec(token.charAt(0)) &&
      token !== 'true' &&
      token !== 'false' &&
      token !== 'this' &&
      token !== paramName &&
      last !== '.'
  }

  /**
   * @param {EventTarget|string} elt
   * @param {string[]} tokens
   * @param {string} paramName
   * @returns {ConditionalFunction|null}
   */
  function maybeGenerateConditional(elt, tokens, paramName) {
    if (tokens[0] === '[') {
      tokens.shift()
      let bracketCount = 1
      let conditionalSource = ' return (function(' + paramName + '){ return ('
      let last = null
      while (tokens.length > 0) {
        const token = tokens[0]
        // @ts-ignore For some reason tsc doesn't understand the shift call, and thinks we're comparing the same value here, i.e. '[' vs ']'
        if (token === ']') {
          bracketCount--
          if (bracketCount === 0) {
            if (last === null) {
              conditionalSource = conditionalSource + 'true'
            }
            tokens.shift()
            conditionalSource += ')})'
            try {
              const conditionFunction = maybeEval(elt, function() {
                return Function(conditionalSource)()
              },
              function() { return true })
              conditionFunction.source = conditionalSource
              return conditionFunction
            } catch (e) {
              triggerErrorEvent(getDocument().body, 'htmx:syntax:error', { error: e, source: conditionalSource })
              return null
            }
          }
        } else if (token === '[') {
          bracketCount++
        }
        if (isPossibleRelativeReference(token, last, paramName)) {
          conditionalSource += '((' + paramName + '.' + token + ') ? (' + paramName + '.' + token + ') : (window.' + token + '))'
        } else {
          conditionalSource = conditionalSource + token
        }
        last = tokens.shift()
      }
    }
  }

  /**
   * @param {string[]} tokens
   * @param {RegExp} match
   * @returns {string}
   */
  function consumeUntil(tokens, match) {
    let result = ''
    while (tokens.length > 0 && !match.test(tokens[0])) {
      result += tokens.shift()
    }
    return result
  }

  /**
   * @param {string[]} tokens
   * @returns {string}
   */
  function consumeCSSSelector(tokens) {
    let result
    if (tokens.length > 0 && COMBINED_SELECTOR_START.test(tokens[0])) {
      tokens.shift()
      result = consumeUntil(tokens, COMBINED_SELECTOR_END).trim()
      tokens.shift()
    } else {
      result = consumeUntil(tokens, WHITESPACE_OR_COMMA)
    }
    return result
  }

  const INPUT_SELECTOR = 'input, textarea, select'

  /**
   * @param {Element} elt
   * @param {string} explicitTrigger
   * @param {Object} cache for trigger specs
   * @returns {HtmxTriggerSpecification[]}
   */
  function parseAndCacheTrigger(elt, explicitTrigger, cache) {
    /** @type HtmxTriggerSpecification[] */
    const triggerSpecs = []
    const tokens = tokenizeString(explicitTrigger)
    do {
      consumeUntil(tokens, NOT_WHITESPACE)
      const initialLength = tokens.length
      const trigger = consumeUntil(tokens, /[,\[\s]/)
      if (trigger !== '') {
        if (trigger === 'every') {
          /** @type HtmxTriggerSpecification */
          const every = { trigger: 'every' }
          consumeUntil(tokens, NOT_WHITESPACE)
          every.pollInterval = parseInterval(consumeUntil(tokens, /[,\[\s]/))
          consumeUntil(tokens, NOT_WHITESPACE)
          var eventFilter = maybeGenerateConditional(elt, tokens, 'event')
          if (eventFilter) {
            every.eventFilter = eventFilter
          }
          triggerSpecs.push(every)
        } else {
          /** @type HtmxTriggerSpecification */
          const triggerSpec = { trigger }
          var eventFilter = maybeGenerateConditional(elt, tokens, 'event')
          if (eventFilter) {
            triggerSpec.eventFilter = eventFilter
          }
          while (tokens.length > 0 && tokens[0] !== ',') {
            consumeUntil(tokens, NOT_WHITESPACE)
            const token = tokens.shift()
            if (token === 'changed') {
              triggerSpec.changed = true
            } else if (token === 'once') {
              triggerSpec.once = true
            } else if (token === 'consume') {
              triggerSpec.consume = true
            } else if (token === 'delay' && tokens[0] === ':') {
              tokens.shift()
              triggerSpec.delay = parseInterval(consumeUntil(tokens, WHITESPACE_OR_COMMA))
            } else if (token === 'from' && tokens[0] === ':') {
              tokens.shift()
              if (COMBINED_SELECTOR_START.test(tokens[0])) {
                var from_arg = consumeCSSSelector(tokens)
              } else {
                var from_arg = consumeUntil(tokens, WHITESPACE_OR_COMMA)
                if (from_arg === 'closest' || from_arg === 'find' || from_arg === 'next' || from_arg === 'previous') {
                  tokens.shift()
                  const selector = consumeCSSSelector(tokens)
                  // `next` and `previous` allow a selector-less syntax
                  if (selector.length > 0) {
                    from_arg += ' ' + selector
                  }
                }
              }
              triggerSpec.from = from_arg
            } else if (token === 'target' && tokens[0] === ':') {
              tokens.shift()
              triggerSpec.target = consumeCSSSelector(tokens)
            } else if (token === 'throttle' && tokens[0] === ':') {
              tokens.shift()
              triggerSpec.throttle = parseInterval(consumeUntil(tokens, WHITESPACE_OR_COMMA))
            } else if (token === 'queue' && tokens[0] === ':') {
              tokens.shift()
              triggerSpec.queue = consumeUntil(tokens, WHITESPACE_OR_COMMA)
            } else if (token === 'root' && tokens[0] === ':') {
              tokens.shift()
              triggerSpec[token] = consumeCSSSelector(tokens)
            } else if (token === 'threshold' && tokens[0] === ':') {
              tokens.shift()
              triggerSpec[token] = consumeUntil(tokens, WHITESPACE_OR_COMMA)
            } else {
              triggerErrorEvent(elt, 'htmx:syntax:error', { token: tokens.shift() })
            }
          }
          triggerSpecs.push(triggerSpec)
        }
      }
      if (tokens.length === initialLength) {
        triggerErrorEvent(elt, 'htmx:syntax:error', { token: tokens.shift() })
      }
      consumeUntil(tokens, NOT_WHITESPACE)
    } while (tokens[0] === ',' && tokens.shift())
    if (cache) {
      cache[explicitTrigger] = triggerSpecs
    }
    return triggerSpecs
  }

  /**
   * @param {Element} elt
   * @returns {HtmxTriggerSpecification[]}
   */
  function getTriggerSpecs(elt) {
    const explicitTrigger = getAttributeValue(elt, 'hx-trigger')
    let triggerSpecs = []
    if (explicitTrigger) {
      const cache = htmx.config.triggerSpecsCache
      triggerSpecs = (cache && cache[explicitTrigger]) || parseAndCacheTrigger(elt, explicitTrigger, cache)
    }

    if (triggerSpecs.length > 0) {
      return triggerSpecs
    } else if (matches(elt, 'form')) {
      return [{ trigger: 'submit' }]
    } else if (matches(elt, 'input[type="button"], input[type="submit"]')) {
      return [{ trigger: 'click' }]
    } else if (matches(elt, INPUT_SELECTOR)) {
      return [{ trigger: 'change' }]
    } else {
      return [{ trigger: 'click' }]
    }
  }

  /**
   * @param {Element} elt
   */
  function cancelPolling(elt) {
    getInternalData(elt).cancelled = true
  }

  /**
   * @param {Element} elt
   * @param {TriggerHandler} handler
   * @param {HtmxTriggerSpecification} spec
   */
  function processPolling(elt, handler, spec) {
    const nodeData = getInternalData(elt)
    nodeData.timeout = getWindow().setTimeout(function() {
      if (bodyContains(elt) && nodeData.cancelled !== true) {
        if (!maybeFilterEvent(spec, elt, makeEvent('hx:poll:trigger', {
          triggerSpec: spec,
          target: elt
        }))) {
          handler(elt)
        }
        processPolling(elt, handler, spec)
      }
    }, spec.pollInterval)
  }

  /**
   * @param {HTMLAnchorElement} elt
   * @returns {boolean}
   */
  function isLocalLink(elt) {
    return location.hostname === elt.hostname &&
      getRawAttribute(elt, 'href') &&
      getRawAttribute(elt, 'href').indexOf('#') !== 0
  }

  /**
   * @param {Element} elt
   */
  function eltIsDisabled(elt) {
    return closest(elt, htmx.config.disableSelector)
  }

  /**
   * @param {Element} elt
   * @param {HtmxNodeInternalData} nodeData
   * @param {HtmxTriggerSpecification[]} triggerSpecs
   */
  function boostElement(elt, nodeData, triggerSpecs) {
    if ((elt instanceof HTMLAnchorElement && isLocalLink(elt) && (elt.target === '' || elt.target === '_self')) || (elt.tagName === 'FORM' && String(getRawAttribute(elt, 'method')).toLowerCase() !== 'dialog')) {
      nodeData.boosted = true
      let verb, path
      if (elt.tagName === 'A') {
        verb = 'get'
        path = getRawAttribute(elt, 'href')
      } else {
        const rawAttribute = getRawAttribute(elt, 'method')
        verb = rawAttribute ? rawAttribute.toLowerCase() : 'get'
        if (verb === 'get') {
        }
        path = getRawAttribute(elt, 'action')
      }
      triggerSpecs.forEach(function(triggerSpec) {
        addEventListener(elt, function(node, evt) {
          const elt = asElement(node)
          if (eltIsDisabled(elt)) {
            cleanUpElement(elt)
            return
          }
          issueAjaxRequest(verb, path, elt, evt)
        }, nodeData, triggerSpec, true)
      })
    }
  }

  /**
   * @param {Event} evt
   * @param {Node} node
   * @returns {boolean}
   */
  function shouldCancel(evt, node) {
    const elt = asElement(node)
    if (!elt) {
      return false
    }
    if (evt.type === 'submit' || evt.type === 'click') {
      if (elt.tagName === 'FORM') {
        return true
      }
      if (matches(elt, 'input[type="submit"], button') && closest(elt, 'form') !== null) {
        return true
      }
      if (elt instanceof HTMLAnchorElement && elt.href &&
        (elt.getAttribute('href') === '#' || elt.getAttribute('href').indexOf('#') !== 0)) {
        return true
      }
    }
    return false
  }

  /**
   * @param {Node} elt
   * @param {Event|MouseEvent|KeyboardEvent|TouchEvent} evt
   * @returns {boolean}
   */
  function ignoreBoostedAnchorCtrlClick(elt, evt) {
    return getInternalData(elt).boosted && elt instanceof HTMLAnchorElement && evt.type === 'click' &&
      // @ts-ignore this will resolve to undefined for events that don't define those properties, which is fine
      (evt.ctrlKey || evt.metaKey)
  }

  /**
   * @param {HtmxTriggerSpecification} triggerSpec
   * @param {Node} elt
   * @param {Event} evt
   * @returns {boolean}
   */
  function maybeFilterEvent(triggerSpec, elt, evt) {
    const eventFilter = triggerSpec.eventFilter
    if (eventFilter) {
      try {
        return eventFilter.call(elt, evt) !== true
      } catch (e) {
        const source = eventFilter.source
        triggerErrorEvent(getDocument().body, 'htmx:eventFilter:error', { error: e, source })
        return true
      }
    }
    return false
  }

  /**
   * @param {Node} elt
   * @param {TriggerHandler} handler
   * @param {HtmxNodeInternalData} nodeData
   * @param {HtmxTriggerSpecification} triggerSpec
   * @param {boolean} [explicitCancel]
   */
  function addEventListener(elt, handler, nodeData, triggerSpec, explicitCancel) {
    const elementData = getInternalData(elt)
    /** @type {(Node|Window)[]} */
    let eltsToListenOn
    if (triggerSpec.from) {
      eltsToListenOn = querySelectorAllExt(elt, triggerSpec.from)
    } else {
      eltsToListenOn = [elt]
    }
    // store the initial values of the elements, so we can tell if they change
    if (triggerSpec.changed) {
      eltsToListenOn.forEach(function(eltToListenOn) {
        const eltToListenOnData = getInternalData(eltToListenOn)
        // @ts-ignore value will be undefined for non-input elements, which is fine
        eltToListenOnData.lastValue = eltToListenOn.value
      })
    }
    forEach(eltsToListenOn, function(eltToListenOn) {
      /** @type EventListener */
      const eventListener = function(evt) {
        if (!bodyContains(elt)) {
          eltToListenOn.removeEventListener(triggerSpec.trigger, eventListener)
          return
        }
        if (ignoreBoostedAnchorCtrlClick(elt, evt)) {
          return
        }
        if (explicitCancel || shouldCancel(evt, elt)) {
          evt.preventDefault()
        }
        if (maybeFilterEvent(triggerSpec, elt, evt)) {
          return
        }
        const eventData = getInternalData(evt)
        eventData.triggerSpec = triggerSpec
        if (eventData.handledFor == null) {
          eventData.handledFor = []
        }
        if (eventData.handledFor.indexOf(elt) < 0) {
          eventData.handledFor.push(elt)
          if (triggerSpec.consume) {
            evt.stopPropagation()
          }
          if (triggerSpec.target && evt.target) {
            if (!matches(asElement(evt.target), triggerSpec.target)) {
              return
            }
          }
          if (triggerSpec.once) {
            if (elementData.triggeredOnce) {
              return
            } else {
              elementData.triggeredOnce = true
            }
          }
          if (triggerSpec.changed) {
            const eltToListenOnData = getInternalData(eltToListenOn)
            // @ts-ignore value will be undefined for non-input elements, which is fine
            const value = eltToListenOn.value
            if (eltToListenOnData.lastValue === value) {
              return
            }
            eltToListenOnData.lastValue = value
          }
          if (elementData.delayed) {
            clearTimeout(elementData.delayed)
          }
          if (elementData.throttle) {
            return
          }

          if (triggerSpec.throttle > 0) {
            if (!elementData.throttle) {
              triggerEvent(elt, 'htmx:trigger')
              handler(elt, evt)
              elementData.throttle = getWindow().setTimeout(function() {
                elementData.throttle = null
              }, triggerSpec.throttle)
            }
          } else if (triggerSpec.delay > 0) {
            elementData.delayed = getWindow().setTimeout(function() {
              triggerEvent(elt, 'htmx:trigger')
              handler(elt, evt)
            }, triggerSpec.delay)
          } else {
            triggerEvent(elt, 'htmx:trigger')
            handler(elt, evt)
          }
        }
      }
      if (nodeData.listenerInfos == null) {
        nodeData.listenerInfos = []
      }
      nodeData.listenerInfos.push({
        trigger: triggerSpec.trigger,
        listener: eventListener,
        on: eltToListenOn
      })
      eltToListenOn.addEventListener(triggerSpec.trigger, eventListener)
    })
  }

  let windowIsScrolling = false // used by initScrollHandler
  let scrollHandler = null
  function initScrollHandler() {
    if (!scrollHandler) {
      scrollHandler = function() {
        windowIsScrolling = true
      }
      window.addEventListener('scroll', scrollHandler)
      setInterval(function() {
        if (windowIsScrolling) {
          windowIsScrolling = false
          forEach(getDocument().querySelectorAll("[hx-trigger*='revealed'],[data-hx-trigger*='revealed']"), function(elt) {
            maybeReveal(elt)
          })
        }
      }, 200)
    }
  }

  /**
   * @param {Element} elt
   */
  function maybeReveal(elt) {
    if (!hasAttribute(elt, 'data-hx-revealed') && isScrolledIntoView(elt)) {
      elt.setAttribute('data-hx-revealed', 'true')
      const nodeData = getInternalData(elt)
      if (nodeData.initHash) {
        triggerEvent(elt, 'revealed')
      } else {
        // if the node isn't initialized, wait for it before triggering the request
        elt.addEventListener('htmx:afterProcessNode', function() { triggerEvent(elt, 'revealed') }, { once: true })
      }
    }
  }

  //= ===================================================================

  /**
   * @param {Element} elt
   * @param {TriggerHandler} handler
   * @param {HtmxNodeInternalData} nodeData
   * @param {number} delay
   */
  function loadImmediately(elt, handler, nodeData, delay) {
    const load = function() {
      if (!nodeData.loaded) {
        nodeData.loaded = true
        handler(elt)
      }
    }
    if (delay > 0) {
      getWindow().setTimeout(load, delay)
    } else {
      load()
    }
  }

  /**
   * @param {Element} elt
   * @param {HtmxNodeInternalData} nodeData
   * @param {HtmxTriggerSpecification[]} triggerSpecs
   * @returns {boolean}
   */
  function processVerbs(elt, nodeData, triggerSpecs) {
    let explicitAction = false
    forEach(VERBS, function(verb) {
      if (hasAttribute(elt, 'hx-' + verb)) {
        const path = getAttributeValue(elt, 'hx-' + verb)
        explicitAction = true
        nodeData.path = path
        nodeData.verb = verb
        triggerSpecs.forEach(function(triggerSpec) {
          addTriggerHandler(elt, triggerSpec, nodeData, function(node, evt) {
            const elt = asElement(node)
            if (closest(elt, htmx.config.disableSelector)) {
              cleanUpElement(elt)
              return
            }
            issueAjaxRequest(verb, path, elt, evt)
          })
        })
      }
    })
    return explicitAction
  }

  /**
   * @callback TriggerHandler
   * @param {Node} elt
   * @param {Event} [evt]
   */

  /**
   * @param {Node} elt
   * @param {HtmxTriggerSpecification} triggerSpec
   * @param {HtmxNodeInternalData} nodeData
   * @param {TriggerHandler} handler
   */
  function addTriggerHandler(elt, triggerSpec, nodeData, handler) {
    if (triggerSpec.trigger === 'revealed') {
      initScrollHandler()
      addEventListener(elt, handler, nodeData, triggerSpec)
      maybeReveal(asElement(elt))
    } else if (triggerSpec.trigger === 'intersect') {
      const observerOptions = {}
      if (triggerSpec.root) {
        observerOptions.root = querySelectorExt(elt, triggerSpec.root)
      }
      if (triggerSpec.threshold) {
        observerOptions.threshold = parseFloat(triggerSpec.threshold)
      }
      const observer = new IntersectionObserver(function(entries) {
        for (let i = 0; i < entries.length; i++) {
          const entry = entries[i]
          if (entry.isIntersecting) {
            triggerEvent(elt, 'intersect')
            break
          }
        }
      }, observerOptions)
      observer.observe(asElement(elt))
      addEventListener(asElement(elt), handler, nodeData, triggerSpec)
    } else if (triggerSpec.trigger === 'load') {
      if (!maybeFilterEvent(triggerSpec, elt, makeEvent('load', { elt }))) {
        loadImmediately(asElement(elt), handler, nodeData, triggerSpec.delay)
      }
    } else if (triggerSpec.pollInterval > 0) {
      nodeData.polling = true
      processPolling(asElement(elt), handler, triggerSpec)
    } else {
      addEventListener(elt, handler, nodeData, triggerSpec)
    }
  }

  /**
   * @param {Node} node
   * @returns {boolean}
   */
  function shouldProcessHxOn(node) {
    const elt = asElement(node)
    if (!elt) {
      return false
    }
    const attributes = elt.attributes
    for (let j = 0; j < attributes.length; j++) {
      const attrName = attributes[j].name
      if (startsWith(attrName, 'hx-on:') || startsWith(attrName, 'data-hx-on:') ||
        startsWith(attrName, 'hx-on-') || startsWith(attrName, 'data-hx-on-')) {
        return true
      }
    }
    return false
  }

  /**
   * @param {Node} elt
   * @returns {Element[]}
   */
  const HX_ON_QUERY = new XPathEvaluator()
    .createExpression('.//*[@*[ starts-with(name(), "hx-on:") or starts-with(name(), "data-hx-on:") or' +
      ' starts-with(name(), "hx-on-") or starts-with(name(), "data-hx-on-") ]]')

  function processHXOnRoot(elt, elements) {
    if (shouldProcessHxOn(elt)) {
      elements.push(asElement(elt))
    }
    const iter = HX_ON_QUERY.evaluate(elt)
    let node = null
    while (node = iter.iterateNext()) elements.push(asElement(node))
  }

  function findHxOnWildcardElements(elt) {
    /** @type {Element[]} */
    const elements = []
    if (elt instanceof DocumentFragment) {
      for (const child of elt.childNodes) {
        processHXOnRoot(child, elements)
      }
    } else {
      processHXOnRoot(elt, elements)
    }
    return elements
  }

  /**
   * @param {Element} elt
   * @returns {NodeListOf<Element>|[]}
   */
  function findElementsToProcess(elt) {
    if (elt.querySelectorAll) {
      const boostedSelector = ', [hx-boost] a, [data-hx-boost] a, a[hx-boost], a[data-hx-boost]'

      const extensionSelectors = []
      for (const e in extensions) {
        const extension = extensions[e]
        if (extension.getSelectors) {
          var selectors = extension.getSelectors()
          if (selectors) {
            extensionSelectors.push(selectors)
          }
        }
      }

      const results = elt.querySelectorAll(VERB_SELECTOR + boostedSelector + ", form, [type='submit']," +
        ' [hx-ext], [data-hx-ext], [hx-trigger], [data-hx-trigger]' + extensionSelectors.flat().map(s => ', ' + s).join(''))

      return results
    } else {
      return []
    }
  }

  /**
   * Handle submit buttons/inputs that have the form attribute set
   * see https://developer.mozilla.org/docs/Web/HTML/Element/button
   * @param {Event} evt
   */
  function maybeSetLastButtonClicked(evt) {
    const elt = /** @type {HTMLButtonElement|HTMLInputElement} */ (closest(asElement(evt.target), "button, input[type='submit']"))
    const internalData = getRelatedFormData(evt)
    if (internalData) {
      internalData.lastButtonClicked = elt
    }
  }

  /**
   * @param {Event} evt
   */
  function maybeUnsetLastButtonClicked(evt) {
    const internalData = getRelatedFormData(evt)
    if (internalData) {
      internalData.lastButtonClicked = null
    }
  }

  /**
   * @param {Event} evt
   * @returns {HtmxNodeInternalData|undefined}
   */
  function getRelatedFormData(evt) {
    const elt = closest(asElement(evt.target), "button, input[type='submit']")
    if (!elt) {
      return
    }
    const form = resolveTarget('#' + getRawAttribute(elt, 'form'), elt.getRootNode()) || closest(elt, 'form')
    if (!form) {
      return
    }
    return getInternalData(form)
  }

  /**
   * @param {EventTarget} elt
   */
  function initButtonTracking(elt) {
    // need to handle both click and focus in:
    //   focusin - in case someone tabs in to a button and hits the space bar
    //   click - on OSX buttons do not focus on click see https://bugs.webkit.org/show_bug.cgi?id=13724
    elt.addEventListener('click', maybeSetLastButtonClicked)
    elt.addEventListener('focusin', maybeSetLastButtonClicked)
    elt.addEventListener('focusout', maybeUnsetLastButtonClicked)
  }

  /**
   * @param {Element} elt
   * @param {string} eventName
   * @param {string} code
   */
  function addHxOnEventHandler(elt, eventName, code) {
    const nodeData = getInternalData(elt)
    if (!Array.isArray(nodeData.onHandlers)) {
      nodeData.onHandlers = []
    }
    let func
    /** @type EventListener */
    const listener = function(e) {
      maybeEval(elt, function() {
        if (eltIsDisabled(elt)) {
          return
        }
        if (!func) {
          func = new Function('event', code)
        }
        func.call(elt, e)
      })
    }
    elt.addEventListener(eventName, listener)
    nodeData.onHandlers.push({ event: eventName, listener })
  }

  /**
   * @param {Element} elt
   */
  function processHxOnWildcard(elt) {
    // wipe any previous on handlers so that this function takes precedence
    deInitOnHandlers(elt)

    for (let i = 0; i < elt.attributes.length; i++) {
      const name = elt.attributes[i].name
      const value = elt.attributes[i].value
      if (startsWith(name, 'hx-on') || startsWith(name, 'data-hx-on')) {
        const afterOnPosition = name.indexOf('-on') + 3
        const nextChar = name.slice(afterOnPosition, afterOnPosition + 1)
        if (nextChar === '-' || nextChar === ':') {
          let eventName = name.slice(afterOnPosition + 1)
          // if the eventName starts with a colon or dash, prepend "htmx" for shorthand support
          if (startsWith(eventName, ':')) {
            eventName = 'htmx' + eventName
          } else if (startsWith(eventName, '-')) {
            eventName = 'htmx:' + eventName.slice(1)
          } else if (startsWith(eventName, 'htmx-')) {
            eventName = 'htmx:' + eventName.slice(5)
          }

          addHxOnEventHandler(elt, eventName, value)
        }
      }
    }
  }

  /**
   * @param {Element|HTMLInputElement} elt
   */
  function initNode(elt) {
    if (closest(elt, htmx.config.disableSelector)) {
      cleanUpElement(elt)
      return
    }
    const nodeData = getInternalData(elt)
    if (nodeData.initHash !== attributeHash(elt)) {
      // clean up any previously processed info
      deInitNode(elt)

      nodeData.initHash = attributeHash(elt)

      triggerEvent(elt, 'htmx:beforeProcessNode')

      // @ts-ignore value will be undefined for non-input elements, which is fine
      if (elt.value) {
        // @ts-ignore
        nodeData.lastValue = elt.value
      }

      const triggerSpecs = getTriggerSpecs(elt)
      const hasExplicitHttpAction = processVerbs(elt, nodeData, triggerSpecs)

      if (!hasExplicitHttpAction) {
        if (getClosestAttributeValue(elt, 'hx-boost') === 'true') {
          boostElement(elt, nodeData, triggerSpecs)
        } else if (hasAttribute(elt, 'hx-trigger')) {
          triggerSpecs.forEach(function(triggerSpec) {
            // For "naked" triggers, don't do anything at all
            addTriggerHandler(elt, triggerSpec, nodeData, function() {
            })
          })
        }
      }

      // Handle submit buttons/inputs that have the form attribute set
      // see https://developer.mozilla.org/docs/Web/HTML/Element/button
      if (elt.tagName === 'FORM' || (getRawAttribute(elt, 'type') === 'submit' && hasAttribute(elt, 'form'))) {
        initButtonTracking(elt)
      }

      triggerEvent(elt, 'htmx:afterProcessNode')
    }
  }

  /**
   * Processes new content, enabling htmx behavior. This can be useful if you have content that is added to the DOM outside of the normal htmx request cycle but still want htmx attributes to work.
   *
   * @see https://htmx.org/api/#process
   *
   * @param {Element|string} elt element to process
   */
  function processNode(elt) {
    elt = resolveTarget(elt)
    console.log("processNode:")
    console.log(elt)
    if (closest(elt, htmx.config.disableSelector)) {
      cleanUpElement(elt)
      return
    }
    initNode(elt)
    forEach(findElementsToProcess(elt), function(child) { initNode(child) })
    forEach(findHxOnWildcardElements(elt), processHxOnWildcard)
  }

  //= ===================================================================
  // Event/Log Support
  //= ===================================================================

  /**
   * @param {string} str
   * @returns {string}
   */
  function kebabEventName(str) {
    return str.replace(/([a-z0-9])([A-Z])/g, '$1-$2').toLowerCase()
  }

  /**
   * @param {string} eventName
   * @param {any} detail
   * @returns {CustomEvent}
   */
  function makeEvent(eventName, detail) {
    let evt
    if (window.CustomEvent && typeof window.CustomEvent === 'function') {
      // TODO: `composed: true` here is a hack to make global event handlers work with events in shadow DOM
      // This breaks expected encapsulation but needs to be here until decided otherwise by core devs
      evt = new CustomEvent(eventName, { bubbles: true, cancelable: true, composed: true, detail })
    } else {
      evt = getDocument().createEvent('CustomEvent')
      evt.initCustomEvent(eventName, true, true, detail)
    }
    return evt
  }

  /**
   * @param {EventTarget|string} elt
   * @param {string} eventName
   * @param {any=} detail
   */
  function triggerErrorEvent(elt, eventName, detail) {
    triggerEvent(elt, eventName, mergeObjects({ error: eventName }, detail))
  }

  /**
   * @param {string} eventName
   * @returns {boolean}
   */
  function ignoreEventForLogging(eventName) {
    return eventName === 'htmx:afterProcessNode'
  }

  /**
   * `withExtensions` locates all active extensions for a provided element, then
   * executes the provided function using each of the active extensions.  It should
   * be called internally at every extendable execution point in htmx.
   *
   * @param {Element} elt
   * @param {(extension:HtmxExtension) => void} toDo
   * @returns void
   */
  function withExtensions(elt, toDo) {
    forEach(getExtensions(elt), function(extension) {
      try {
        toDo(extension)
      } catch (e) {
        logError(e)
      }
    })
  }

  function logError(msg) {
    if (console.error) {
      console.error(msg)
    } else if (console.log) {
      console.log('ERROR: ', msg)
    }
  }

  /**
   * Triggers a given event on an element
   *
   * @see https://htmx.org/api/#trigger
   *
   * @param {EventTarget|string} elt the element to trigger the event on
   * @param {string} eventName the name of the event to trigger
   * @param {any=} detail details for the event
   * @returns {boolean}
   */
  function triggerEvent(elt, eventName, detail) {
    elt = resolveTarget(elt)
    if (detail == null) {
      detail = {}
    }
    detail.elt = elt
    const event = makeEvent(eventName, detail)
    if (htmx.logger && !ignoreEventForLogging(eventName)) {
      htmx.logger(elt, eventName, detail)
    }
    if (detail.error) {
      logError(detail.error)
      triggerEvent(elt, 'htmx:error', { errorInfo: detail })
    }
    let eventResult = elt.dispatchEvent(event)
    const kebabName = kebabEventName(eventName)
    if (eventResult && kebabName !== eventName) {
      const kebabedEvent = makeEvent(kebabName, event.detail)
      eventResult = eventResult && elt.dispatchEvent(kebabedEvent)
    }
    withExtensions(asElement(elt), function(extension) {
      eventResult = eventResult && (extension.onEvent(eventName, event) !== false && !event.defaultPrevented)
    })
    return eventResult
  }

  //= ===================================================================
  // History Support
  //= ===================================================================
  let currentPathForHistory = location.pathname + location.search

  /**
   * @returns {Element}
   */
  function getHistoryElement() {
    const historyElt = getDocument().querySelector('[hx-history-elt],[data-hx-history-elt]')
    return historyElt || getDocument().body
  }

  /**
   * @param {string} url
   * @param {Element} rootElt
   */
  function saveToHistoryCache(url, rootElt) {
    if (!canAccessLocalStorage()) {
      return
    }

    // get state to save
    const innerHTML = cleanInnerHtmlForHistory(rootElt)
    const title = getDocument().title
    const scroll = window.scrollY

    if (htmx.config.historyCacheSize <= 0) {
      // make sure that an eventually already existing cache is purged
      localStorage.removeItem('htmx-history-cache')
      return
    }

    url = normalizePath(url)

    const historyCache = parseJSON(localStorage.getItem('htmx-history-cache')) || []
    for (let i = 0; i < historyCache.length; i++) {
      if (historyCache[i].url === url) {
        historyCache.splice(i, 1)
        break
      }
    }

    /** @type HtmxHistoryItem */
    const newHistoryItem = { url, content: innerHTML, title, scroll }

    triggerEvent(getDocument().body, 'htmx:historyItemCreated', { item: newHistoryItem, cache: historyCache })

    historyCache.push(newHistoryItem)
    while (historyCache.length > htmx.config.historyCacheSize) {
      historyCache.shift()
    }

    // keep trying to save the cache until it succeeds or is empty
    while (historyCache.length > 0) {
      try {
        localStorage.setItem('htmx-history-cache', JSON.stringify(historyCache))
        break
      } catch (e) {
        triggerErrorEvent(getDocument().body, 'htmx:historyCacheError', { cause: e, cache: historyCache })
        historyCache.shift() // shrink the cache and retry
      }
    }
  }

  /**
   * @typedef {Object} HtmxHistoryItem
   * @property {string} url
   * @property {string} content
   * @property {string} title
   * @property {number} scroll
   */

  /**
   * @param {string} url
   * @returns {HtmxHistoryItem|null}
   */
  function getCachedHistory(url) {
    if (!canAccessLocalStorage()) {
      return null
    }

    url = normalizePath(url)

    const historyCache = parseJSON(localStorage.getItem('htmx-history-cache')) || []
    for (let i = 0; i < historyCache.length; i++) {
      if (historyCache[i].url === url) {
        return historyCache[i]
      }
    }
    return null
  }

  /**
   * @param {Element} elt
   * @returns {string}
   */
  function cleanInnerHtmlForHistory(elt) {
    const className = htmx.config.requestClass
    const clone = /** @type Element */ (elt.cloneNode(true))
    forEach(findAll(clone, '.' + className), function(child) {
      removeClassFromElement(child, className)
    })
    // remove the disabled attribute for any element disabled due to an htmx request
    forEach(findAll(clone, '[data-disabled-by-htmx]'), function(child) {
      child.removeAttribute('disabled')
    })
    return clone.innerHTML
  }

  function saveCurrentPageToHistory() {
    const elt = getHistoryElement()
    const path = currentPathForHistory || location.pathname + location.search

    // Allow history snapshot feature to be disabled where hx-history="false"
    // is present *anywhere* in the current document we're about to save,
    // so we can prevent privileged data entering the cache.
    // The page will still be reachable as a history entry, but htmx will fetch it
    // live from the server onpopstate rather than look in the localStorage cache
    let disableHistoryCache
    try {
      disableHistoryCache = getDocument().querySelector('[hx-history="false" i],[data-hx-history="false" i]')
    } catch (e) {
    // IE11: insensitive modifier not supported so fallback to case sensitive selector
      disableHistoryCache = getDocument().querySelector('[hx-history="false"],[data-hx-history="false"]')
    }
    if (!disableHistoryCache) {
      triggerEvent(getDocument().body, 'htmx:beforeHistorySave', { path, historyElt: elt })
      saveToHistoryCache(path, elt)
    }

    if (htmx.config.historyEnabled) history.replaceState({ htmx: true }, getDocument().title, window.location.href)
  }

  /**
   * @param {string} path
   */
  function pushUrlIntoHistory(path) {
  // remove the cache buster parameter, if any
    if (htmx.config.getCacheBusterParam) {
      path = path.replace(/org\.htmx\.cache-buster=[^&]*&?/, '')
      if (endsWith(path, '&') || endsWith(path, '?')) {
        path = path.slice(0, -1)
      }
    }
    if (htmx.config.historyEnabled) {
      history.pushState({ htmx: true }, '', path)
    }
    currentPathForHistory = path
  }

  /**
   * @param {string} path
   */
  function replaceUrlInHistory(path) {
    if (htmx.config.historyEnabled) history.replaceState({ htmx: true }, '', path)
    currentPathForHistory = path
  }

  /**
   * @param {HtmxSettleTask[]} tasks
   */
  function settleImmediately(tasks) {
    forEach(tasks, function(task) {
      task.call(undefined)
    })
  }

  /**
   * @param {string} path
   */
  function loadHistoryFromServer(path) {
    const request = new XMLHttpRequest()
    const details = { path, xhr: request }
    triggerEvent(getDocument().body, 'htmx:historyCacheMiss', details)
    request.open('GET', path, true)
    request.setRequestHeader('HX-Request', 'true')
    request.setRequestHeader('HX-History-Restore-Request', 'true')
    request.setRequestHeader('HX-Current-URL', getDocument().location.href)
    request.onload = function() {
      if (this.status >= 200 && this.status < 400) {
        triggerEvent(getDocument().body, 'htmx:historyCacheMissLoad', details)
        const fragment = makeFragment(this.response)
        /** @type ParentNode */
        const content = fragment.querySelector('[hx-history-elt],[data-hx-history-elt]') || fragment
        const historyElement = getHistoryElement()
        const settleInfo = makeSettleInfo(historyElement)
        handleTitle(fragment.title)

        swapInnerHTML(historyElement, content, settleInfo)
        settleImmediately(settleInfo.tasks)
        currentPathForHistory = path
        triggerEvent(getDocument().body, 'htmx:historyRestore', { path, cacheMiss: true, serverResponse: this.response })
      } else {
        triggerErrorEvent(getDocument().body, 'htmx:historyCacheMissLoadError', details)
      }
    }
    request.send()
  }

  /**
   * @param {string} [path]
   */
  function restoreHistory(path) {
    saveCurrentPageToHistory()
    path = path || location.pathname + location.search
    const cached = getCachedHistory(path)
    if (cached) {
      const fragment = makeFragment(cached.content)
      const historyElement = getHistoryElement()
      const settleInfo = makeSettleInfo(historyElement)
      handleTitle(fragment.title)
      swapInnerHTML(historyElement, fragment, settleInfo)
      settleImmediately(settleInfo.tasks)
      getWindow().setTimeout(function() {
        window.scrollTo(0, cached.scroll)
      }, 0) // next 'tick', so browser has time to render layout
      currentPathForHistory = path
      triggerEvent(getDocument().body, 'htmx:historyRestore', { path, item: cached })
    } else {
      if (htmx.config.refreshOnHistoryMiss) {
        // @ts-ignore: optional parameter in reload() function throws error
        // noinspection JSUnresolvedReference
        window.location.reload(true)
      } else {
        loadHistoryFromServer(path)
      }
    }
  }

  /**
   * @param {Element} elt
   * @returns {Element[]}
   */
  function addRequestIndicatorClasses(elt) {
    let indicators = /** @type Element[] */ (findAttributeTargets(elt, 'hx-indicator'))
    if (indicators == null) {
      indicators = [elt]
    }
    forEach(indicators, function(ic) {
      const internalData = getInternalData(ic)
      internalData.requestCount = (internalData.requestCount || 0) + 1
      ic.classList.add.call(ic.classList, htmx.config.requestClass)
    })
    return indicators
  }

  /**
   * @param {Element} elt
   * @returns {Element[]}
   */
  function disableElements(elt) {
    let disabledElts = /** @type Element[] */ (findAttributeTargets(elt, 'hx-disabled-elt'))
    if (disabledElts == null) {
      disabledElts = []
    }
    forEach(disabledElts, function(disabledElement) {
      const internalData = getInternalData(disabledElement)
      internalData.requestCount = (internalData.requestCount || 0) + 1
      disabledElement.setAttribute('disabled', '')
      disabledElement.setAttribute('data-disabled-by-htmx', '')
    })
    return disabledElts
  }

  /**
   * @param {Element[]} indicators
   * @param {Element[]} disabled
   */
  function removeRequestIndicators(indicators, disabled) {
    forEach(indicators, function(ic) {
      const internalData = getInternalData(ic)
      internalData.requestCount = (internalData.requestCount || 0) - 1
      if (internalData.requestCount === 0) {
        ic.classList.remove.call(ic.classList, htmx.config.requestClass)
      }
    })
    forEach(disabled, function(disabledElement) {
      const internalData = getInternalData(disabledElement)
      internalData.requestCount = (internalData.requestCount || 0) - 1
      if (internalData.requestCount === 0) {
        disabledElement.removeAttribute('disabled')
        disabledElement.removeAttribute('data-disabled-by-htmx')
      }
    })
  }

  //= ===================================================================
  // Input Value Processing
  //= ===================================================================

  /**
   * @param {Element[]} processed
   * @param {Element} elt
   * @returns {boolean}
   */
  function haveSeenNode(processed, elt) {
    for (let i = 0; i < processed.length; i++) {
      const node = processed[i]
      if (node.isSameNode(elt)) {
        return true
      }
    }
    return false
  }

  /**
   * @param {Element} element
   * @return {boolean}
   */
  function shouldInclude(element) {
    // Cast to trick tsc, undefined values will work fine here
    const elt = /** @type {HTMLInputElement} */ (element)
    if (elt.name === '' || elt.name == null || elt.disabled || closest(elt, 'fieldset[disabled]')) {
      return false
    }
    // ignore "submitter" types (see jQuery src/serialize.js)
    if (elt.type === 'button' || elt.type === 'submit' || elt.tagName === 'image' || elt.tagName === 'reset' || elt.tagName === 'file') {
      return false
    }
    if (elt.type === 'checkbox' || elt.type === 'radio') {
      return elt.checked
    }
    return true
  }

  /** @param {string} name
   * @param {string|Array|FormDataEntryValue} value
   * @param {FormData} formData */
  function addValueToFormData(name, value, formData) {
    if (name != null && value != null) {
      if (Array.isArray(value)) {
        value.forEach(function(v) { formData.append(name, v) })
      } else {
        formData.append(name, value)
      }
    }
  }

  /** @param {string} name
   * @param {string|Array} value
   * @param {FormData} formData */
  function removeValueFromFormData(name, value, formData) {
    if (name != null && value != null) {
      let values = formData.getAll(name)
      if (Array.isArray(value)) {
        values = values.filter(v => value.indexOf(v) < 0)
      } else {
        values = values.filter(v => v !== value)
      }
      formData.delete(name)
      forEach(values, v => formData.append(name, v))
    }
  }

  /**
   * @param {Element[]} processed
   * @param {FormData} formData
   * @param {HtmxElementValidationError[]} errors
   * @param {Element|HTMLInputElement|HTMLSelectElement|HTMLFormElement} elt
   * @param {boolean} validate
   */
  function processInputValue(processed, formData, errors, elt, validate) {
    if (elt == null || haveSeenNode(processed, elt)) {
      return
    } else {
      processed.push(elt)
    }
    if (shouldInclude(elt)) {
      const name = getRawAttribute(elt, 'name')
      // @ts-ignore value will be undefined for non-input elements, which is fine
      let value = elt.value
      if (elt instanceof HTMLSelectElement && elt.multiple) {
        value = toArray(elt.querySelectorAll('option:checked')).map(function(e) { return (/** @type HTMLOptionElement */(e)).value })
      }
      // include file inputs
      if (elt instanceof HTMLInputElement && elt.files) {
        value = toArray(elt.files)
      }
      addValueToFormData(name, value, formData)
      if (validate) {
        validateElement(elt, errors)
      }
    }
    if (elt instanceof HTMLFormElement) {
      forEach(elt.elements, function(input) {
        if (processed.indexOf(input) >= 0) {
          // The input has already been processed and added to the values, but the FormData that will be
          //  constructed right after on the form, will include it once again. So remove that input's value
          //  now to avoid duplicates
          removeValueFromFormData(input.name, input.value, formData)
        } else {
          processed.push(input)
        }
        if (validate) {
          validateElement(input, errors)
        }
      })
      new FormData(elt).forEach(function(value, name) {
        if (value instanceof File && value.name === '') {
          return // ignore no-name files
        }
        addValueToFormData(name, value, formData)
      })
    }
  }

  /**
   *
   * @param {Element} elt
   * @param {HtmxElementValidationError[]} errors
   */
  function validateElement(elt, errors) {
    const element = /** @type {HTMLElement & ElementInternals} */ (elt)
    if (element.willValidate) {
      triggerEvent(element, 'htmx:validation:validate')
      if (!element.checkValidity()) {
        errors.push({ elt: element, message: element.validationMessage, validity: element.validity })
        triggerEvent(element, 'htmx:validation:failed', { message: element.validationMessage, validity: element.validity })
      }
    }
  }

  /**
   * Override values in the one FormData with those from another.
   * @param {FormData} receiver the formdata that will be mutated
   * @param {FormData} donor the formdata that will provide the overriding values
   * @returns {FormData} the {@linkcode receiver}
   */
  function overrideFormData(receiver, donor) {
    for (const key of donor.keys()) {
      receiver.delete(key)
    }
    donor.forEach(function(value, key) {
      receiver.append(key, value)
    })
    return receiver
  }

  /**
 * @param {Element|HTMLFormElement} elt
 * @param {HttpVerb} verb
 * @returns {{errors: HtmxElementValidationError[], formData: FormData, values: Object}}
 */
  function getInputValues(elt, verb) {
    /** @type Element[] */
    const processed = []
    const formData = new FormData()
    const priorityFormData = new FormData()
    /** @type HtmxElementValidationError[] */
    const errors = []
    const internalData = getInternalData(elt)
    if (internalData.lastButtonClicked && !bodyContains(internalData.lastButtonClicked)) {
      internalData.lastButtonClicked = null
    }

    // only validate when form is directly submitted and novalidate or formnovalidate are not set
    // or if the element has an explicit hx-validate="true" on it
    let validate = (elt instanceof HTMLFormElement && elt.noValidate !== true) || getAttributeValue(elt, 'hx-validate') === 'true'
    if (internalData.lastButtonClicked) {
      validate = validate && internalData.lastButtonClicked.formNoValidate !== true
    }

    // for a non-GET include the closest form
    if (verb !== 'get') {
      processInputValue(processed, priorityFormData, errors, closest(elt, 'form'), validate)
    }

    // include the element itself
    processInputValue(processed, formData, errors, elt, validate)

    // if a button or submit was clicked last, include its value
    if (internalData.lastButtonClicked || elt.tagName === 'BUTTON' ||
    (elt.tagName === 'INPUT' && getRawAttribute(elt, 'type') === 'submit')) {
      const button = internalData.lastButtonClicked || (/** @type HTMLInputElement|HTMLButtonElement */(elt))
      const name = getRawAttribute(button, 'name')
      addValueToFormData(name, button.value, priorityFormData)
    }

    // include any explicit includes
    const includes = findAttributeTargets(elt, 'hx-include')
    forEach(includes, function(node) {
      processInputValue(processed, formData, errors, asElement(node), validate)
      // if a non-form is included, include any input values within it
      if (!matches(node, 'form')) {
        forEach(asParentNode(node).querySelectorAll(INPUT_SELECTOR), function(descendant) {
          processInputValue(processed, formData, errors, descendant, validate)
        })
      }
    })

    // values from a <form> take precedence, overriding the regular values
    overrideFormData(formData, priorityFormData)

    return { errors, formData, values: formDataProxy(formData) }
  }

  /**
   * @param {string} returnStr
   * @param {string} name
   * @param {any} realValue
   * @returns {string}
   */
  function appendParam(returnStr, name, realValue) {
    if (returnStr !== '') {
      returnStr += '&'
    }
    if (String(realValue) === '[object Object]') {
      realValue = JSON.stringify(realValue)
    }
    const s = encodeURIComponent(realValue)
    returnStr += encodeURIComponent(name) + '=' + s
    return returnStr
  }

  /**
   * @param {FormData|Object} values
   * @returns string
   */
  function urlEncode(values) {
    values = formDataFromObject(values)
    let returnStr = ''
    values.forEach(function(value, key) {
      returnStr = appendParam(returnStr, key, value)
    })
    return returnStr
  }

  //= ===================================================================
  // Ajax
  //= ===================================================================

  /**
 * @param {Element} elt
 * @param {Element} target
 * @param {string} prompt
 * @returns {HtmxHeaderSpecification}
 */
  function getHeaders(elt, target, prompt) {
    /** @type HtmxHeaderSpecification */
    const headers = {
      'HX-Request': 'true',
      'HX-Trigger': getRawAttribute(elt, 'id'),
      'HX-Trigger-Name': getRawAttribute(elt, 'name'),
      'HX-Target': getAttributeValue(target, 'id'),
      'HX-Current-URL': getDocument().location.href
    }
    getValuesForElement(elt, 'hx-headers', false, headers)
    if (prompt !== undefined) {
      headers['HX-Prompt'] = prompt
    }
    if (getInternalData(elt).boosted) {
      headers['HX-Boosted'] = 'true'
    }
    return headers
  }

  /**
 * filterValues takes an object containing form input values
 * and returns a new object that only contains keys that are
 * specified by the closest "hx-params" attribute
 * @param {FormData} inputValues
 * @param {Element} elt
 * @returns {FormData}
 */
  function filterValues(inputValues, elt) {
    const paramsValue = getClosestAttributeValue(elt, 'hx-params')
    if (paramsValue) {
      if (paramsValue === 'none') {
        return new FormData()
      } else if (paramsValue === '*') {
        return inputValues
      } else if (paramsValue.indexOf('not ') === 0) {
        forEach(paramsValue.substr(4).split(','), function(name) {
          name = name.trim()
          inputValues.delete(name)
        })
        return inputValues
      } else {
        const newValues = new FormData()
        forEach(paramsValue.split(','), function(name) {
          name = name.trim()
          if (inputValues.has(name)) {
            inputValues.getAll(name).forEach(function(value) { newValues.append(name, value) })
          }
        })
        return newValues
      }
    } else {
      return inputValues
    }
  }

  /**
   * @param {Element} elt
   * @return {boolean}
   */
  function isAnchorLink(elt) {
    return !!getRawAttribute(elt, 'href') && getRawAttribute(elt, 'href').indexOf('#') >= 0
  }

  /**
 * @param {Element} elt
 * @param {HtmxSwapStyle} [swapInfoOverride]
 * @returns {HtmxSwapSpecification}
 */
  function getSwapSpecification(elt, swapInfoOverride) {
    const swapInfo = swapInfoOverride || getClosestAttributeValue(elt, 'hx-swap')
    /** @type HtmxSwapSpecification */
    const swapSpec = {
      swapStyle: getInternalData(elt).boosted ? 'innerHTML' : htmx.config.defaultSwapStyle,
      swapDelay: htmx.config.defaultSwapDelay,
      settleDelay: htmx.config.defaultSettleDelay
    }
    if (htmx.config.scrollIntoViewOnBoost && getInternalData(elt).boosted && !isAnchorLink(elt)) {
      swapSpec.show = 'top'
    }
    if (swapInfo) {
      const split = splitOnWhitespace(swapInfo)
      if (split.length > 0) {
        for (let i = 0; i < split.length; i++) {
          const value = split[i]
          if (value.indexOf('swap:') === 0) {
            swapSpec.swapDelay = parseInterval(value.substr(5))
          } else if (value.indexOf('settle:') === 0) {
            swapSpec.settleDelay = parseInterval(value.substr(7))
          } else if (value.indexOf('transition:') === 0) {
            swapSpec.transition = value.substr(11) === 'true'
          } else if (value.indexOf('ignoreTitle:') === 0) {
            swapSpec.ignoreTitle = value.substr(12) === 'true'
          } else if (value.indexOf('scroll:') === 0) {
            const scrollSpec = value.substr(7)
            var splitSpec = scrollSpec.split(':')
            const scrollVal = splitSpec.pop()
            var selectorVal = splitSpec.length > 0 ? splitSpec.join(':') : null
            // @ts-ignore
            swapSpec.scroll = scrollVal
            swapSpec.scrollTarget = selectorVal
          } else if (value.indexOf('show:') === 0) {
            const showSpec = value.substr(5)
            var splitSpec = showSpec.split(':')
            const showVal = splitSpec.pop()
            var selectorVal = splitSpec.length > 0 ? splitSpec.join(':') : null
            swapSpec.show = showVal
            swapSpec.showTarget = selectorVal
          } else if (value.indexOf('focus-scroll:') === 0) {
            const focusScrollVal = value.substr('focus-scroll:'.length)
            swapSpec.focusScroll = focusScrollVal == 'true'
          } else if (i == 0) {
            swapSpec.swapStyle = value
          } else {
            logError('Unknown modifier in hx-swap: ' + value)
          }
        }
      }
    }
    return swapSpec
  }

  /**
   * @param {Element} elt
   * @return {boolean}
   */
  function usesFormData(elt) {
    return getClosestAttributeValue(elt, 'hx-encoding') === 'multipart/form-data' ||
    (matches(elt, 'form') && getRawAttribute(elt, 'enctype') === 'multipart/form-data')
  }

  /**
   * @param {XMLHttpRequest} xhr
   * @param {Element} elt
   * @param {FormData} filteredParameters
   * @returns {*|string|null}
   */
  function encodeParamsForBody(xhr, elt, filteredParameters) {
    let encodedParameters = null
    withExtensions(elt, function(extension) {
      if (encodedParameters == null) {
        encodedParameters = extension.encodeParameters(xhr, filteredParameters, elt)
      }
    })
    if (encodedParameters != null) {
      return encodedParameters
    } else {
      if (usesFormData(elt)) {
        // Force conversion to an actual FormData object in case filteredParameters is a formDataProxy
        // See https://github.com/bigskysoftware/htmx/issues/2317
        return overrideFormData(new FormData(), formDataFromObject(filteredParameters))
      } else {
        return urlEncode(filteredParameters)
      }
    }
  }

  /**
 *
 * @param {Element} target
 * @returns {HtmxSettleInfo}
 */
  function makeSettleInfo(target) {
    return { tasks: [], elts: [target] }
  }

  /**
   * @param {Element[]} content
   * @param {HtmxSwapSpecification} swapSpec
   */
  function updateScrollState(content, swapSpec) {
    const first = content[0]
    const last = content[content.length - 1]
    if (swapSpec.scroll) {
      var target = null
      if (swapSpec.scrollTarget) {
        target = asElement(querySelectorExt(first, swapSpec.scrollTarget))
      }
      if (swapSpec.scroll === 'top' && (first || target)) {
        target = target || first
        target.scrollTop = 0
      }
      if (swapSpec.scroll === 'bottom' && (last || target)) {
        target = target || last
        target.scrollTop = target.scrollHeight
      }
    }
    if (swapSpec.show) {
      var target = null
      if (swapSpec.showTarget) {
        let targetStr = swapSpec.showTarget
        if (swapSpec.showTarget === 'window') {
          targetStr = 'body'
        }
        target = asElement(querySelectorExt(first, targetStr))
      }
      if (swapSpec.show === 'top' && (first || target)) {
        target = target || first
        // @ts-ignore For some reason tsc doesn't recognize "instant" as a valid option for now
        target.scrollIntoView({ block: 'start', behavior: htmx.config.scrollBehavior })
      }
      if (swapSpec.show === 'bottom' && (last || target)) {
        target = target || last
        // @ts-ignore For some reason tsc doesn't recognize "instant" as a valid option for now
        target.scrollIntoView({ block: 'end', behavior: htmx.config.scrollBehavior })
      }
    }
  }

  /**
 * @param {Element} elt
 * @param {string} attr
 * @param {boolean=} evalAsDefault
 * @param {Object=} values
 * @returns {Object}
 */
  function getValuesForElement(elt, attr, evalAsDefault, values) {
    if (values == null) {
      values = {}
    }
    if (elt == null) {
      return values
    }
    const attributeValue = getAttributeValue(elt, attr)
    if (attributeValue) {
      let str = attributeValue.trim()
      let evaluateValue = evalAsDefault
      if (str === 'unset') {
        return null
      }
      if (str.indexOf('javascript:') === 0) {
        str = str.substr(11)
        evaluateValue = true
      } else if (str.indexOf('js:') === 0) {
        str = str.substr(3)
        evaluateValue = true
      }
      if (str.indexOf('{') !== 0) {
        str = '{' + str + '}'
      }
      let varsValues
      if (evaluateValue) {
        varsValues = maybeEval(elt, function() { return Function('return (' + str + ')')() }, {})
      } else {
        varsValues = parseJSON(str)
      }
      for (const key in varsValues) {
        if (varsValues.hasOwnProperty(key)) {
          if (values[key] == null) {
            values[key] = varsValues[key]
          }
        }
      }
    }
    return getValuesForElement(asElement(parentElt(elt)), attr, evalAsDefault, values)
  }

  /**
   * @param {EventTarget|string} elt
   * @param {() => any} toEval
   * @param {any=} defaultVal
   * @returns {any}
   */
  function maybeEval(elt, toEval, defaultVal) {
    if (htmx.config.allowEval) {
      return toEval()
    } else {
      triggerErrorEvent(elt, 'htmx:evalDisallowedError')
      return defaultVal
    }
  }

  /**
 * @param {Element} elt
 * @param {*?} expressionVars
 * @returns
 */
  function getHXVarsForElement(elt, expressionVars) {
    return getValuesForElement(elt, 'hx-vars', true, expressionVars)
  }

  /**
 * @param {Element} elt
 * @param {*?} expressionVars
 * @returns
 */
  function getHXValsForElement(elt, expressionVars) {
    return getValuesForElement(elt, 'hx-vals', false, expressionVars)
  }

  /**
 * @param {Element} elt
 * @returns {FormData}
 */
  function getExpressionVars(elt) {
    return mergeObjects(getHXVarsForElement(elt), getHXValsForElement(elt))
  }

  /**
   * @param {XMLHttpRequest} xhr
   * @param {string} header
   * @param {string|null} headerValue
   */
  function safelySetHeaderValue(xhr, header, headerValue) {
    if (headerValue !== null) {
      try {
        xhr.setRequestHeader(header, headerValue)
      } catch (e) {
      // On an exception, try to set the header URI encoded instead
        xhr.setRequestHeader(header, encodeURIComponent(headerValue))
        xhr.setRequestHeader(header + '-URI-AutoEncoded', 'true')
      }
    }
  }

  /**
   * @param {XMLHttpRequest} xhr
   * @return {string}
   */
  function getPathFromResponse(xhr) {
  // NB: IE11 does not support this stuff
    if (xhr.responseURL && typeof (URL) !== 'undefined') {
      try {
        const url = new URL(xhr.responseURL)
        return url.pathname + url.search
      } catch (e) {
        triggerErrorEvent(getDocument().body, 'htmx:badResponseUrl', { url: xhr.responseURL })
      }
    }
  }

  /**
   * @param {XMLHttpRequest} xhr
   * @param {RegExp} regexp
   * @return {boolean}
   */
  function hasHeader(xhr, regexp) {
    return regexp.test(xhr.getAllResponseHeaders())
  }

  /**
   * Issues an htmx-style AJAX request
   *
   * @see https://htmx.org/api/#ajax
   *
   * @param {HttpVerb} verb
   * @param {string} path the URL path to make the AJAX
   * @param {Element|string|HtmxAjaxHelperContext} context the element to target (defaults to the **body**) | a selector for the target | a context object that contains any of the following
   * @return {Promise<void>} Promise that resolves immediately if no request is sent, or when the request is complete
   */
  function ajaxHelper(verb, path, context) {
    verb = (/** @type HttpVerb */(verb.toLowerCase()))
    if (context) {
      if (context instanceof Element || typeof context === 'string') {
        return issueAjaxRequest(verb, path, null, null, {
          targetOverride: resolveTarget(context),
          returnPromise: true
        })
      } else {
        return issueAjaxRequest(verb, path, resolveTarget(context.source), context.event,
          {
            handler: context.handler,
            headers: context.headers,
            values: context.values,
            targetOverride: resolveTarget(context.target),
            swapOverride: context.swap,
            select: context.select,
            returnPromise: true
          })
      }
    } else {
      return issueAjaxRequest(verb, path, null, null, {
        returnPromise: true
      })
    }
  }

  /**
   * @param {Element} elt
   * @return {Element[]}
   */
  function hierarchyForElt(elt) {
    const arr = []
    while (elt) {
      arr.push(elt)
      elt = elt.parentElement
    }
    return arr
  }

  /**
   * @param {Element} elt
   * @param {string} path
   * @param {HtmxRequestConfig} requestConfig
   * @return {boolean}
   */
  function verifyPath(elt, path, requestConfig) {
    let sameHost
    let url
    if (typeof URL === 'function') {
      url = new URL(path, document.location.href)
      const origin = document.location.origin
      sameHost = origin === url.origin
    } else {
    // IE11 doesn't support URL
      url = path
      sameHost = startsWith(path, document.location.origin)
    }

    if (htmx.config.selfRequestsOnly) {
      if (!sameHost) {
        return false
      }
    }
    return triggerEvent(elt, 'htmx:validateUrl', mergeObjects({ url, sameHost }, requestConfig))
  }

  /**
   * @param {Object|FormData} obj
   * @return {FormData}
   */
  function formDataFromObject(obj) {
    if (obj instanceof FormData) return obj
    const formData = new FormData()
    for (const key in obj) {
      if (obj.hasOwnProperty(key)) {
        if (typeof obj[key].forEach === 'function') {
          obj[key].forEach(function(v) { formData.append(key, v) })
        } else if (typeof obj[key] === 'object' && !(obj[key] instanceof Blob)) {
          formData.append(key, JSON.stringify(obj[key]))
        } else {
          formData.append(key, obj[key])
        }
      }
    }
    return formData
  }

  /**
   * @param {FormData} formData
   * @param {string} name
   * @param {Array} array
   * @returns {Array}
   */
  function formDataArrayProxy(formData, name, array) {
    // mutating the array should mutate the underlying form data
    return new Proxy(array, {
      get: function(target, key) {
        if (typeof key === 'number') return target[key]
        if (key === 'length') return target.length
        if (key === 'push') {
          return function(value) {
            target.push(value)
            formData.append(name, value)
          }
        }
        if (typeof target[key] === 'function') {
          return function() {
            target[key].apply(target, arguments)
            formData.delete(name)
            target.forEach(function(v) { formData.append(name, v) })
          }
        }

        if (target[key] && target[key].length === 1) {
          return target[key][0]
        } else {
          return target[key]
        }
      },
      set: function(target, index, value) {
        target[index] = value
        formData.delete(name)
        target.forEach(function(v) { formData.append(name, v) })
        return true
      }
    })
  }

  /**
   * @param {FormData} formData
   * @returns {Object}
   */
  function formDataProxy(formData) {
    return new Proxy(formData, {
      get: function(target, name) {
        if (typeof name === 'symbol') {
          // Forward symbol calls to the FormData itself directly
          return Reflect.get(target, name)
        }
        if (name === 'toJSON') {
          // Support JSON.stringify call on proxy
          return () => Object.fromEntries(formData)
        }
        if (name in target) {
          // Wrap in function with apply to correctly bind the FormData context, as a direct call would result in an illegal invocation error
          if (typeof target[name] === 'function') {
            return function() {
              return formData[name].apply(formData, arguments)
            }
          } else {
            return target[name]
          }
        }
        const array = formData.getAll(name)
        // Those 2 undefined & single value returns are for retro-compatibility as we weren't using FormData before
        if (array.length === 0) {
          return undefined
        } else if (array.length === 1) {
          return array[0]
        } else {
          return formDataArrayProxy(target, name, array)
        }
      },
      set: function(target, name, value) {
        if (typeof name !== 'string') {
          return false
        }
        target.delete(name)
        if (typeof value.forEach === 'function') {
          value.forEach(function(v) { target.append(name, v) })
        } else if (typeof value === 'object' && !(value instanceof Blob)) {
          target.append(name, JSON.stringify(value))
        } else {
          target.append(name, value)
        }
        return true
      },
      deleteProperty: function(target, name) {
        if (typeof name === 'string') {
          target.delete(name)
        }
        return true
      },
      // Support Object.assign call from proxy
      ownKeys: function(target) {
        return Reflect.ownKeys(Object.fromEntries(target))
      },
      getOwnPropertyDescriptor: function(target, prop) {
        return Reflect.getOwnPropertyDescriptor(Object.fromEntries(target), prop)
      }
    })
  }

  /**
   * @param {HttpVerb} verb
   * @param {string} path
   * @param {Element} elt
   * @param {Event} event
   * @param {HtmxAjaxEtc} [etc]
   * @param {boolean} [confirmed]
   * @return {Promise<void>}
   */
  function issueAjaxRequest(verb, path, elt, event, etc, confirmed) {
    let resolve = null
    let reject = null
    etc = etc != null ? etc : {}
    if (etc.returnPromise && typeof Promise !== 'undefined') {
      var promise = new Promise(function(_resolve, _reject) {
        resolve = _resolve
        reject = _reject
      })
    }
    if (elt == null) {
      elt = getDocument().body
    }
    const responseHandler = etc.handler || handleAjaxResponse
    const select = etc.select || null

    if (!bodyContains(elt)) {
    // do not issue requests for elements removed from the DOM
      maybeCall(resolve)
      return promise
    }
    const target = etc.targetOverride || asElement(getTarget(elt))
    if (target == null || target == DUMMY_ELT) {
      triggerErrorEvent(elt, 'htmx:targetError', { target: getAttributeValue(elt, 'hx-target') })
      maybeCall(reject)
      return promise
    }

    let eltData = getInternalData(elt)
    const submitter = eltData.lastButtonClicked

    if (submitter) {
      const buttonPath = getRawAttribute(submitter, 'formaction')
      if (buttonPath != null) {
        path = buttonPath
      }

      const buttonVerb = getRawAttribute(submitter, 'formmethod')
      if (buttonVerb != null) {
      // ignore buttons with formmethod="dialog"
        if (buttonVerb.toLowerCase() !== 'dialog') {
          verb = (/** @type HttpVerb */(buttonVerb))
        }
      }
    }

    const confirmQuestion = getClosestAttributeValue(elt, 'hx-confirm')
    // allow event-based confirmation w/ a callback
    if (confirmed === undefined) {
      const issueRequest = function(skipConfirmation) {
        return issueAjaxRequest(verb, path, elt, event, etc, !!skipConfirmation)
      }
      const confirmDetails = { target, elt, path, verb, triggeringEvent: event, etc, issueRequest, question: confirmQuestion }
      if (triggerEvent(elt, 'htmx:confirm', confirmDetails) === false) {
        maybeCall(resolve)
        return promise
      }
    }

    let syncElt = elt
    let syncStrategy = getClosestAttributeValue(elt, 'hx-sync')
    let queueStrategy = null
    let abortable = false
    if (syncStrategy) {
      const syncStrings = syncStrategy.split(':')
      const selector = syncStrings[0].trim()
      if (selector === 'this') {
        syncElt = findThisElement(elt, 'hx-sync')
      } else {
        syncElt = asElement(querySelectorExt(elt, selector))
      }
      // default to the drop strategy
      syncStrategy = (syncStrings[1] || 'drop').trim()
      eltData = getInternalData(syncElt)
      if (syncStrategy === 'drop' && eltData.xhr && eltData.abortable !== true) {
        maybeCall(resolve)
        return promise
      } else if (syncStrategy === 'abort') {
        if (eltData.xhr) {
          maybeCall(resolve)
          return promise
        } else {
          abortable = true
        }
      } else if (syncStrategy === 'replace') {
        triggerEvent(syncElt, 'htmx:abort') // abort the current request and continue
      } else if (syncStrategy.indexOf('queue') === 0) {
        const queueStrArray = syncStrategy.split(' ')
        queueStrategy = (queueStrArray[1] || 'last').trim()
      }
    }

    if (eltData.xhr) {
      if (eltData.abortable) {
        triggerEvent(syncElt, 'htmx:abort') // abort the current request and continue
      } else {
        if (queueStrategy == null) {
          if (event) {
            const eventData = getInternalData(event)
            if (eventData && eventData.triggerSpec && eventData.triggerSpec.queue) {
              queueStrategy = eventData.triggerSpec.queue
            }
          }
          if (queueStrategy == null) {
            queueStrategy = 'last'
          }
        }
        if (eltData.queuedRequests == null) {
          eltData.queuedRequests = []
        }
        if (queueStrategy === 'first' && eltData.queuedRequests.length === 0) {
          eltData.queuedRequests.push(function() {
            issueAjaxRequest(verb, path, elt, event, etc)
          })
        } else if (queueStrategy === 'all') {
          eltData.queuedRequests.push(function() {
            issueAjaxRequest(verb, path, elt, event, etc)
          })
        } else if (queueStrategy === 'last') {
          eltData.queuedRequests = [] // dump existing queue
          eltData.queuedRequests.push(function() {
            issueAjaxRequest(verb, path, elt, event, etc)
          })
        }
        maybeCall(resolve)
        return promise
      }
    }

    const xhr = new XMLHttpRequest()
    eltData.xhr = xhr
    eltData.abortable = abortable
    const endRequestLock = function() {
      eltData.xhr = null
      eltData.abortable = false
      if (eltData.queuedRequests != null &&
      eltData.queuedRequests.length > 0) {
        const queuedRequest = eltData.queuedRequests.shift()
        queuedRequest()
      }
    }
    const promptQuestion = getClosestAttributeValue(elt, 'hx-prompt')
    if (promptQuestion) {
      var promptResponse = prompt(promptQuestion)
      // prompt returns null if cancelled and empty string if accepted with no entry
      if (promptResponse === null ||
      !triggerEvent(elt, 'htmx:prompt', { prompt: promptResponse, target })) {
        maybeCall(resolve)
        endRequestLock()
        return promise
      }
    }

    if (confirmQuestion && !confirmed) {
      if (!confirm(confirmQuestion)) {
        maybeCall(resolve)
        endRequestLock()
        return promise
      }
    }

    let headers = getHeaders(elt, target, promptResponse)

    if (verb !== 'get' && !usesFormData(elt)) {
      headers['Content-Type'] = 'application/x-www-form-urlencoded'
    }

    if (etc.headers) {
      headers = mergeObjects(headers, etc.headers)
    }
    const results = getInputValues(elt, verb)
    let errors = results.errors
    const rawFormData = results.formData
    if (etc.values) {
      overrideFormData(rawFormData, formDataFromObject(etc.values))
    }
    const expressionVars = formDataFromObject(getExpressionVars(elt))
    const allFormData = overrideFormData(rawFormData, expressionVars)
    let filteredFormData = filterValues(allFormData, elt)

    if (htmx.config.getCacheBusterParam && verb === 'get') {
      filteredFormData.set('org.htmx.cache-buster', getRawAttribute(target, 'id') || 'true')
    }

    // behavior of anchors w/ empty href is to use the current URL
    if (path == null || path === '') {
      path = getDocument().location.href
    }

    /**
     * @type {Object}
     * @property {boolean} [credentials]
     * @property {number} [timeout]
     * @property {boolean} [noHeaders]
     */
    const requestAttrValues = getValuesForElement(elt, 'hx-request')

    const eltIsBoosted = getInternalData(elt).boosted

    let useUrlParams = htmx.config.methodsThatUseUrlParams.indexOf(verb) >= 0

    /** @type HtmxRequestConfig */
    const requestConfig = {
      boosted: eltIsBoosted,
      useUrlParams,
      formData: filteredFormData,
      parameters: formDataProxy(filteredFormData),
      unfilteredFormData: allFormData,
      unfilteredParameters: formDataProxy(allFormData),
      headers,
      target,
      verb,
      errors,
      withCredentials: etc.credentials || requestAttrValues.credentials || htmx.config.withCredentials,
      timeout: etc.timeout || requestAttrValues.timeout || htmx.config.timeout,
      path,
      triggeringEvent: event
    }

    if (!triggerEvent(elt, 'htmx:configRequest', requestConfig)) {
      maybeCall(resolve)
      endRequestLock()
      return promise
    }

    // copy out in case the object was overwritten
    path = requestConfig.path
    verb = requestConfig.verb
    headers = requestConfig.headers
    filteredFormData = formDataFromObject(requestConfig.parameters)
    errors = requestConfig.errors
    useUrlParams = requestConfig.useUrlParams

    if (errors && errors.length > 0) {
      triggerEvent(elt, 'htmx:validation:halted', requestConfig)
      maybeCall(resolve)
      endRequestLock()
      return promise
    }

    const splitPath = path.split('#')
    const pathNoAnchor = splitPath[0]
    const anchor = splitPath[1]

    let finalPath = path
    if (useUrlParams) {
      finalPath = pathNoAnchor
      const hasValues = !filteredFormData.keys().next().done
      if (hasValues) {
        if (finalPath.indexOf('?') < 0) {
          finalPath += '?'
        } else {
          finalPath += '&'
        }
        finalPath += urlEncode(filteredFormData)
        if (anchor) {
          finalPath += '#' + anchor
        }
      }
    }

    if (!verifyPath(elt, finalPath, requestConfig)) {
      triggerErrorEvent(elt, 'htmx:invalidPath', requestConfig)
      maybeCall(reject)
      return promise
    }

    xhr.open(verb.toUpperCase(), finalPath, true)
    xhr.overrideMimeType('text/html')
    xhr.withCredentials = requestConfig.withCredentials
    xhr.timeout = requestConfig.timeout

    // request headers
    if (requestAttrValues.noHeaders) {
    // ignore all headers
    } else {
      for (const header in headers) {
        if (headers.hasOwnProperty(header)) {
          const headerValue = headers[header]
          safelySetHeaderValue(xhr, header, headerValue)
        }
      }
    }

    /** @type {HtmxResponseInfo} */
    const responseInfo = {
      xhr,
      target,
      requestConfig,
      etc,
      boosted: eltIsBoosted,
      select,
      pathInfo: {
        requestPath: path,
        finalRequestPath: finalPath,
        responsePath: null,
        anchor
      }
    }

    xhr.onload = function() {
      try {
        const hierarchy = hierarchyForElt(elt)
        responseInfo.pathInfo.responsePath = getPathFromResponse(xhr)
        responseHandler(elt, responseInfo)
        if (responseInfo.keepIndicators !== true) {
          removeRequestIndicators(indicators, disableElts)
        }
        triggerEvent(elt, 'htmx:afterRequest', responseInfo)
        triggerEvent(elt, 'htmx:afterOnLoad', responseInfo)
        // if the body no longer contains the element, trigger the event on the closest parent
        // remaining in the DOM
        if (!bodyContains(elt)) {
          let secondaryTriggerElt = null
          while (hierarchy.length > 0 && secondaryTriggerElt == null) {
            const parentEltInHierarchy = hierarchy.shift()
            if (bodyContains(parentEltInHierarchy)) {
              secondaryTriggerElt = parentEltInHierarchy
            }
          }
          if (secondaryTriggerElt) {
            triggerEvent(secondaryTriggerElt, 'htmx:afterRequest', responseInfo)
            triggerEvent(secondaryTriggerElt, 'htmx:afterOnLoad', responseInfo)
          }
        }
        maybeCall(resolve)
        endRequestLock()
      } catch (e) {
        triggerErrorEvent(elt, 'htmx:onLoadError', mergeObjects({ error: e }, responseInfo))
        throw e
      }
    }
    xhr.onerror = function() {
      removeRequestIndicators(indicators, disableElts)
      triggerErrorEvent(elt, 'htmx:afterRequest', responseInfo)
      triggerErrorEvent(elt, 'htmx:sendError', responseInfo)
      maybeCall(reject)
      endRequestLock()
    }
    xhr.onabort = function() {
      removeRequestIndicators(indicators, disableElts)
      triggerErrorEvent(elt, 'htmx:afterRequest', responseInfo)
      triggerErrorEvent(elt, 'htmx:sendAbort', responseInfo)
      maybeCall(reject)
      endRequestLock()
    }
    xhr.ontimeout = function() {
      removeRequestIndicators(indicators, disableElts)
      triggerErrorEvent(elt, 'htmx:afterRequest', responseInfo)
      triggerErrorEvent(elt, 'htmx:timeout', responseInfo)
      maybeCall(reject)
      endRequestLock()
    }
    if (!triggerEvent(elt, 'htmx:beforeRequest', responseInfo)) {
      maybeCall(resolve)
      endRequestLock()
      return promise
    }
    var indicators = addRequestIndicatorClasses(elt)
    var disableElts = disableElements(elt)

    forEach(['loadstart', 'loadend', 'progress', 'abort'], function(eventName) {
      forEach([xhr, xhr.upload], function(target) {
        target.addEventListener(eventName, function(event) {
          triggerEvent(elt, 'htmx:xhr:' + eventName, {
            lengthComputable: event.lengthComputable,
            loaded: event.loaded,
            total: event.total
          })
        })
      })
    })
    triggerEvent(elt, 'htmx:beforeSend', responseInfo)
    const params = useUrlParams ? null : encodeParamsForBody(xhr, elt, filteredFormData)
    xhr.send(params)
    return promise
  }

  /**
   * @typedef {Object} HtmxHistoryUpdate
   * @property {string|null} [type]
   * @property {string|null} [path]
   */

  /**
   * @param {Element} elt
   * @param {HtmxResponseInfo} responseInfo
   * @return {HtmxHistoryUpdate}
   */
  function determineHistoryUpdates(elt, responseInfo) {
    const xhr = responseInfo.xhr

    //= ==========================================
    // First consult response headers
    //= ==========================================
    let pathFromHeaders = null
    let typeFromHeaders = null
    if (hasHeader(xhr, /HX-Push:/i)) {
      pathFromHeaders = xhr.getResponseHeader('HX-Push')
      typeFromHeaders = 'push'
    } else if (hasHeader(xhr, /HX-Push-Url:/i)) {
      pathFromHeaders = xhr.getResponseHeader('HX-Push-Url')
      typeFromHeaders = 'push'
    } else if (hasHeader(xhr, /HX-Replace-Url:/i)) {
      pathFromHeaders = xhr.getResponseHeader('HX-Replace-Url')
      typeFromHeaders = 'replace'
    }

    // if there was a response header, that has priority
    if (pathFromHeaders) {
      if (pathFromHeaders === 'false') {
        return {}
      } else {
        return {
          type: typeFromHeaders,
          path: pathFromHeaders
        }
      }
    }

    //= ==========================================
    // Next resolve via DOM values
    //= ==========================================
    const requestPath = responseInfo.pathInfo.finalRequestPath
    const responsePath = responseInfo.pathInfo.responsePath

    const pushUrl = getClosestAttributeValue(elt, 'hx-push-url')
    const replaceUrl = getClosestAttributeValue(elt, 'hx-replace-url')
    const elementIsBoosted = getInternalData(elt).boosted

    let saveType = null
    let path = null

    if (pushUrl) {
      saveType = 'push'
      path = pushUrl
    } else if (replaceUrl) {
      saveType = 'replace'
      path = replaceUrl
    } else if (elementIsBoosted) {
      saveType = 'push'
      path = responsePath || requestPath // if there is no response path, go with the original request path
    }

    if (path) {
    // false indicates no push, return empty object
      if (path === 'false') {
        return {}
      }

      // true indicates we want to follow wherever the server ended up sending us
      if (path === 'true') {
        path = responsePath || requestPath // if there is no response path, go with the original request path
      }

      // restore any anchor associated with the request
      if (responseInfo.pathInfo.anchor && path.indexOf('#') === -1) {
        path = path + '#' + responseInfo.pathInfo.anchor
      }

      return {
        type: saveType,
        path
      }
    } else {
      return {}
    }
  }

  /**
   * @param {HtmxResponseHandlingConfig} responseHandlingConfig
   * @param {number} status
   * @return {boolean}
   */
  function codeMatches(responseHandlingConfig, status) {
    var regExp = new RegExp(responseHandlingConfig.code)
    return regExp.test(status.toString(10))
  }

  /**
   * @param {XMLHttpRequest} xhr
   * @return {HtmxResponseHandlingConfig}
   */
  function resolveResponseHandling(xhr) {
    for (var i = 0; i < htmx.config.responseHandling.length; i++) {
      /** @type HtmxResponseHandlingConfig */
      var responseHandlingElement = htmx.config.responseHandling[i]
      if (codeMatches(responseHandlingElement, xhr.status)) {
        return responseHandlingElement
      }
    }
    // no matches, return no swap
    return {
      swap: false
    }
  }

  /**
   * @param {string} title
   */
  function handleTitle(title) {
    if (title) {
      const titleElt = find('title')
      if (titleElt) {
        titleElt.innerHTML = title
      } else {
        window.document.title = title
      }
    }
  }

  /**
   * @param {Element} elt
   * @param {HtmxResponseInfo} responseInfo
   */
  function handleAjaxResponse(elt, responseInfo) {
    const xhr = responseInfo.xhr
    let target = responseInfo.target
    const etc = responseInfo.etc
    const responseInfoSelect = responseInfo.select

    if (!triggerEvent(elt, 'htmx:beforeOnLoad', responseInfo)) return

    if (hasHeader(xhr, /HX-Trigger:/i)) {
      handleTriggerHeader(xhr, 'HX-Trigger', elt)
    }

    if (hasHeader(xhr, /HX-Location:/i)) {
      saveCurrentPageToHistory()
      let redirectPath = xhr.getResponseHeader('HX-Location')
      /** @type {HtmxAjaxHelperContext&{path:string}} */
      var redirectSwapSpec
      if (redirectPath.indexOf('{') === 0) {
        redirectSwapSpec = parseJSON(redirectPath)
        // what's the best way to throw an error if the user didn't include this
        redirectPath = redirectSwapSpec.path
        delete redirectSwapSpec.path
      }
      ajaxHelper('get', redirectPath, redirectSwapSpec).then(function() {
        pushUrlIntoHistory(redirectPath)
      })
      return
    }

    const shouldRefresh = hasHeader(xhr, /HX-Refresh:/i) && xhr.getResponseHeader('HX-Refresh') === 'true'

    if (hasHeader(xhr, /HX-Redirect:/i)) {
      responseInfo.keepIndicators = true
      location.href = xhr.getResponseHeader('HX-Redirect')
      shouldRefresh && location.reload()
      return
    }

    if (shouldRefresh) {
      responseInfo.keepIndicators = true
      location.reload()
      return
    }

    if (hasHeader(xhr, /HX-Retarget:/i)) {
      if (xhr.getResponseHeader('HX-Retarget') === 'this') {
        responseInfo.target = elt
      } else {
        responseInfo.target = asElement(querySelectorExt(elt, xhr.getResponseHeader('HX-Retarget')))
      }
    }

    const historyUpdate = determineHistoryUpdates(elt, responseInfo)

    const responseHandling = resolveResponseHandling(xhr)
    const shouldSwap = responseHandling.swap
    let isError = !!responseHandling.error
    let ignoreTitle = htmx.config.ignoreTitle || responseHandling.ignoreTitle
    let selectOverride = responseHandling.select
    if (responseHandling.target) {
      responseInfo.target = asElement(querySelectorExt(elt, responseHandling.target))
    }
    var swapOverride = etc.swapOverride
    if (swapOverride == null && responseHandling.swapOverride) {
      swapOverride = responseHandling.swapOverride
    }

    // response headers override response handling config
    if (hasHeader(xhr, /HX-Retarget:/i)) {
      if (xhr.getResponseHeader('HX-Retarget') === 'this') {
        responseInfo.target = elt
      } else {
        responseInfo.target = asElement(querySelectorExt(elt, xhr.getResponseHeader('HX-Retarget')))
      }
    }
    if (hasHeader(xhr, /HX-Reswap:/i)) {
      swapOverride = xhr.getResponseHeader('HX-Reswap')
    }

    var serverResponse = xhr.response
    /** @type HtmxBeforeSwapDetails */
    var beforeSwapDetails = mergeObjects({
      shouldSwap,
      serverResponse,
      isError,
      ignoreTitle,
      selectOverride
    }, responseInfo)

    if (responseHandling.event && !triggerEvent(target, responseHandling.event, beforeSwapDetails)) return

    if (!triggerEvent(target, 'htmx:beforeSwap', beforeSwapDetails)) return

    target = beforeSwapDetails.target // allow re-targeting
    serverResponse = beforeSwapDetails.serverResponse // allow updating content
    isError = beforeSwapDetails.isError // allow updating error
    ignoreTitle = beforeSwapDetails.ignoreTitle // allow updating ignoring title
    selectOverride = beforeSwapDetails.selectOverride // allow updating select override

    responseInfo.target = target // Make updated target available to response events
    responseInfo.failed = isError // Make failed property available to response events
    responseInfo.successful = !isError // Make successful property available to response events

    if (beforeSwapDetails.shouldSwap) {
      if (xhr.status === 286) {
        cancelPolling(elt)
      }

      withExtensions(elt, function(extension) {
        serverResponse = extension.transformResponse(serverResponse, xhr, elt)
      })

      // Save current page if there will be a history update
      if (historyUpdate.type) {
        saveCurrentPageToHistory()
      }

      if (hasHeader(xhr, /HX-Reswap:/i)) {
        swapOverride = xhr.getResponseHeader('HX-Reswap')
      }
      var swapSpec = getSwapSpecification(elt, swapOverride)

      if (!swapSpec.hasOwnProperty('ignoreTitle')) {
        swapSpec.ignoreTitle = ignoreTitle
      }

      target.classList.add(htmx.config.swappingClass)

      // optional transition API promise callbacks
      let settleResolve = null
      let settleReject = null

      if (responseInfoSelect) {
        selectOverride = responseInfoSelect
      }

      if (hasHeader(xhr, /HX-Reselect:/i)) {
        selectOverride = xhr.getResponseHeader('HX-Reselect')
      }

      const selectOOB = getClosestAttributeValue(elt, 'hx-select-oob')
      const select = getClosestAttributeValue(elt, 'hx-select')

      let doSwap = function() {
        try {
          // if we need to save history, do so, before swapping so that relative resources have the correct base URL
          if (historyUpdate.type) {
            triggerEvent(getDocument().body, 'htmx:beforeHistoryUpdate', mergeObjects({ history: historyUpdate }, responseInfo))
            if (historyUpdate.type === 'push') {
              pushUrlIntoHistory(historyUpdate.path)
              triggerEvent(getDocument().body, 'htmx:pushedIntoHistory', { path: historyUpdate.path })
            } else {
              replaceUrlInHistory(historyUpdate.path)
              triggerEvent(getDocument().body, 'htmx:replacedInHistory', { path: historyUpdate.path })
            }
          }

          swap(target, serverResponse, swapSpec, {
            select: selectOverride || select,
            selectOOB,
            eventInfo: responseInfo,
            anchor: responseInfo.pathInfo.anchor,
            contextElement: elt,
            afterSwapCallback: function() {
              if (hasHeader(xhr, /HX-Trigger-After-Swap:/i)) {
                let finalElt = elt
                if (!bodyContains(elt)) {
                  finalElt = getDocument().body
                }
                handleTriggerHeader(xhr, 'HX-Trigger-After-Swap', finalElt)
              }
            },
            afterSettleCallback: function() {
              if (hasHeader(xhr, /HX-Trigger-After-Settle:/i)) {
                let finalElt = elt
                if (!bodyContains(elt)) {
                  finalElt = getDocument().body
                }
                handleTriggerHeader(xhr, 'HX-Trigger-After-Settle', finalElt)
              }
              maybeCall(settleResolve)
            }
          })
        } catch (e) {
          triggerErrorEvent(elt, 'htmx:swapError', responseInfo)
          maybeCall(settleReject)
          throw e
        }
      }

      let shouldTransition = htmx.config.globalViewTransitions
      if (swapSpec.hasOwnProperty('transition')) {
        shouldTransition = swapSpec.transition
      }

      if (shouldTransition &&
              triggerEvent(elt, 'htmx:beforeTransition', responseInfo) &&
              typeof Promise !== 'undefined' &&
              // @ts-ignore experimental feature atm
              document.startViewTransition) {
        const settlePromise = new Promise(function(_resolve, _reject) {
          settleResolve = _resolve
          settleReject = _reject
        })
        // wrap the original doSwap() in a call to startViewTransition()
        const innerDoSwap = doSwap
        doSwap = function() {
          // @ts-ignore experimental feature atm
          document.startViewTransition(function() {
            innerDoSwap()
            return settlePromise
          })
        }
      }

      if (swapSpec.swapDelay > 0) {
        getWindow().setTimeout(doSwap, swapSpec.swapDelay)
      } else {
        doSwap()
      }
    }
    if (isError) {
      triggerErrorEvent(elt, 'htmx:responseError', mergeObjects({ error: 'Response Status Error Code ' + xhr.status + ' from ' + responseInfo.pathInfo.requestPath }, responseInfo))
    }
  }

  //= ===================================================================
  // Extensions API
  //= ===================================================================

  /** @type {Object<string, HtmxExtension>} */
  const extensions = {}

  /**
   * extensionBase defines the default functions for all extensions.
   * @returns {HtmxExtension}
   */
  function extensionBase() {
    return {
      init: function(api) { return null },
      getSelectors: function() { return null },
      onEvent: function(name, evt) { return true },
      transformResponse: function(text, xhr, elt) { return text },
      isInlineSwap: function(swapStyle) { return false },
      handleSwap: function(swapStyle, target, fragment, settleInfo) { return false },
      encodeParameters: function(xhr, parameters, elt) { return null }
    }
  }

  /**
   * defineExtension initializes the extension and adds it to the htmx registry
   *
   * @see https://htmx.org/api/#defineExtension
   *
   * @param {string} name the extension name
   * @param {HtmxExtension} extension the extension definition
   */
  function defineExtension(name, extension) {
    if (extension.init) {
      extension.init(internalAPI)
    }
    extensions[name] = mergeObjects(extensionBase(), extension)
  }

  /**
   * removeExtension removes an extension from the htmx registry
   *
   * @see https://htmx.org/api/#removeExtension
   *
   * @param {string} name
   */
  function removeExtension(name) {
    delete extensions[name]
  }

  /**
   * getExtensions searches up the DOM tree to return all extensions that can be applied to a given element
   *
   * @param {Element} elt
   * @param {HtmxExtension[]=} extensionsToReturn
   * @param {string[]=} extensionsToIgnore
   * @returns {HtmxExtension[]}
   */
  function getExtensions(elt, extensionsToReturn, extensionsToIgnore) {
    if (extensionsToReturn == undefined) {
      extensionsToReturn = []
    }
    if (elt == undefined) {
      return extensionsToReturn
    }
    if (extensionsToIgnore == undefined) {
      extensionsToIgnore = []
    }
    const extensionsForElement = getAttributeValue(elt, 'hx-ext')
    if (extensionsForElement) {
      forEach(extensionsForElement.split(','), function(extensionName) {
        extensionName = extensionName.replace(/ /g, '')
        if (extensionName.slice(0, 7) == 'ignore:') {
          extensionsToIgnore.push(extensionName.slice(7))
          return
        }
        if (extensionsToIgnore.indexOf(extensionName) < 0) {
          const extension = extensions[extensionName]
          if (extension && extensionsToReturn.indexOf(extension) < 0) {
            extensionsToReturn.push(extension)
          }
        }
      })
    }
    return getExtensions(asElement(parentElt(elt)), extensionsToReturn, extensionsToIgnore)
  }

  //= ===================================================================
  // Initialization
  //= ===================================================================
  var isReady = false
  getDocument().addEventListener('DOMContentLoaded', function() {
    isReady = true
  })

  /**
   * Execute a function now if DOMContentLoaded has fired, otherwise listen for it.
   *
   * This function uses isReady because there is no reliable way to ask the browser whether
   * the DOMContentLoaded event has already been fired; there's a gap between DOMContentLoaded
   * firing and readystate=complete.
   */
  function ready(fn) {
    // Checking readyState here is a failsafe in case the htmx script tag entered the DOM by
    // some means other than the initial page load.
    if (isReady || getDocument().readyState === 'complete') {
      fn()
    } else {
      getDocument().addEventListener('DOMContentLoaded', fn)
    }
  }

  function insertIndicatorStyles() {
    if (htmx.config.includeIndicatorStyles !== false) {
      const nonceAttribute = htmx.config.inlineStyleNonce ? ` nonce="${htmx.config.inlineStyleNonce}"` : ''
      getDocument().head.insertAdjacentHTML('beforeend',
        '<style' + nonceAttribute + '>\
      .' + htmx.config.indicatorClass + '{opacity:0}\
      .' + htmx.config.requestClass + ' .' + htmx.config.indicatorClass + '{opacity:1; transition: opacity 200ms ease-in;}\
      .' + htmx.config.requestClass + '.' + htmx.config.indicatorClass + '{opacity:1; transition: opacity 200ms ease-in;}\
      </style>')
    }
  }

  function getMetaConfig() {
    /** @type HTMLMetaElement */
    const element = getDocument().querySelector('meta[name="htmx-config"]')
    if (element) {
      return parseJSON(element.content)
    } else {
      return null
    }
  }

  function mergeMetaConfig() {
    const metaConfig = getMetaConfig()
    if (metaConfig) {
      htmx.config = mergeObjects(htmx.config, metaConfig)
    }
  }

  // initialize the document
  ready(function() {
    mergeMetaConfig()
    insertIndicatorStyles()
    let body = getDocument().body
    processNode(body)
    const restoredElts = getDocument().querySelectorAll(
      "[hx-trigger='restored'],[data-hx-trigger='restored']"
    )
    body.addEventListener('htmx:abort', function(evt) {
      const target = evt.target
      const internalData = getInternalData(target)
      if (internalData && internalData.xhr) {
        internalData.xhr.abort()
      }
    })
    /** @type {(ev: PopStateEvent) => any} */
    const originalPopstate = window.onpopstate ? window.onpopstate.bind(window) : null
    /** @type {(ev: PopStateEvent) => any} */
    window.onpopstate = function(event) {
      if (event.state && event.state.htmx) {
        restoreHistory()
        forEach(restoredElts, function(elt) {
          triggerEvent(elt, 'htmx:restored', {
            document: getDocument(),
            triggerEvent
          })
        })
      } else {
        if (originalPopstate) {
          originalPopstate(event)
        }
      }
    }
    getWindow().setTimeout(function() {
      triggerEvent(body, 'htmx:load', {}) // give ready handlers a chance to load up before firing this event
      body = null // kill reference for gc
    }, 0)
  })

  return htmx
})()

/** @typedef {'get'|'head'|'post'|'put'|'delete'|'connect'|'options'|'trace'|'patch'} HttpVerb */

/**
 * @typedef {Object} SwapOptions
 * @property {string} [select]
 * @property {string} [selectOOB]
 * @property {*} [eventInfo]
 * @property {string} [anchor]
 * @property {Element} [contextElement]
 * @property {swapCallback} [afterSwapCallback]
 * @property {swapCallback} [afterSettleCallback]
 */

/**
 * @callback swapCallback
 */

/**
 * @typedef {'innerHTML' | 'outerHTML' | 'beforebegin' | 'afterbegin' | 'beforeend' | 'afterend' | 'delete' | 'none' | string} HtmxSwapStyle
 */

/**
 * @typedef HtmxSwapSpecification
 * @property {HtmxSwapStyle} swapStyle
 * @property {number} swapDelay
 * @property {number} settleDelay
 * @property {boolean} [transition]
 * @property {boolean} [ignoreTitle]
 * @property {string} [head]
 * @property {'top' | 'bottom'} [scroll]
 * @property {string} [scrollTarget]
 * @property {string} [show]
 * @property {string} [showTarget]
 * @property {boolean} [focusScroll]
 */

/**
 * @typedef {((this:Node, evt:Event) => boolean) & {source: string}} ConditionalFunction
 */

/**
 * @typedef {Object} HtmxTriggerSpecification
 * @property {string} trigger
 * @property {number} [pollInterval]
 * @property {ConditionalFunction} [eventFilter]
 * @property {boolean} [changed]
 * @property {boolean} [once]
 * @property {boolean} [consume]
 * @property {number} [delay]
 * @property {string} [from]
 * @property {string} [target]
 * @property {number} [throttle]
 * @property {string} [queue]
 * @property {string} [root]
 * @property {string} [threshold]
 */

/**
 * @typedef {{elt: Element, message: string, validity: ValidityState}} HtmxElementValidationError
 */

/**
 * @typedef {Record<string, string>} HtmxHeaderSpecification
 * @property {'true'} HX-Request
 * @property {string|null} HX-Trigger
 * @property {string|null} HX-Trigger-Name
 * @property {string|null} HX-Target
 * @property {string} HX-Current-URL
 * @property {string} [HX-Prompt]
 * @property {'true'} [HX-Boosted]
 * @property {string} [Content-Type]
 * @property {'true'} [HX-History-Restore-Request]
 */

/** @typedef HtmxAjaxHelperContext
 * @property {Element|string} [source]
 * @property {Event} [event]
 * @property {HtmxAjaxHandler} [handler]
 * @property {Element|string} [target]
 * @property {HtmxSwapStyle} [swap]
 * @property {Object|FormData} [values]
 * @property {Record<string,string>} [headers]
 * @property {string} [select]
 */

/**
 * @typedef {Object} HtmxRequestConfig
 * @property {boolean} boosted
 * @property {boolean} useUrlParams
 * @property {FormData} formData
 * @property {Object} parameters formData proxy
 * @property {FormData} unfilteredFormData
 * @property {Object} unfilteredParameters unfilteredFormData proxy
 * @property {HtmxHeaderSpecification} headers
 * @property {Element} target
 * @property {HttpVerb} verb
 * @property {HtmxElementValidationError[]} errors
 * @property {boolean} withCredentials
 * @property {number} timeout
 * @property {string} path
 * @property {Event} triggeringEvent
 */

/**
 * @typedef {Object} HtmxResponseInfo
 * @property {XMLHttpRequest} xhr
 * @property {Element} target
 * @property {HtmxRequestConfig} requestConfig
 * @property {HtmxAjaxEtc} etc
 * @property {boolean} boosted
 * @property {string} select
 * @property {{requestPath: string, finalRequestPath: string, responsePath: string|null, anchor: string}} pathInfo
 * @property {boolean} [failed]
 * @property {boolean} [successful]
 * @property {boolean} [keepIndicators]
 */

/**
 * @typedef {Object} HtmxAjaxEtc
 * @property {boolean} [returnPromise]
 * @property {HtmxAjaxHandler} [handler]
 * @property {string} [select]
 * @property {Element} [targetOverride]
 * @property {HtmxSwapStyle} [swapOverride]
 * @property {Record<string,string>} [headers]
 * @property {Object|FormData} [values]
 * @property {boolean} [credentials]
 * @property {number} [timeout]
 */

/**
 * @typedef {Object} HtmxResponseHandlingConfig
 * @property {string} [code]
 * @property {boolean} swap
 * @property {boolean} [error]
 * @property {boolean} [ignoreTitle]
 * @property {string} [select]
 * @property {string} [target]
 * @property {string} [swapOverride]
 * @property {string} [event]
 */

/**
 * @typedef {HtmxResponseInfo & {shouldSwap: boolean, serverResponse: any, isError: boolean, ignoreTitle: boolean, selectOverride:string}} HtmxBeforeSwapDetails
 */

/**
 * @callback HtmxAjaxHandler
 * @param {Element} elt
 * @param {HtmxResponseInfo} responseInfo
 */

/**
 * @typedef {(() => void)} HtmxSettleTask
 */

/**
 * @typedef {Object} HtmxSettleInfo
 * @property {HtmxSettleTask[]} tasks
 * @property {Element[]} elts
 * @property {string} [title]
 */

/**
 * @see https://github.com/bigskysoftware/htmx-extensions/blob/main/README.md
 * @typedef {Object} HtmxExtension
 * @property {(api: any) => void} init
 * @property {(name: string, event: Event|CustomEvent) => boolean} onEvent
 * @property {(text: string, xhr: XMLHttpRequest, elt: Element) => string} transformResponse
 * @property {(swapStyle: HtmxSwapStyle) => boolean} isInlineSwap
 * @property {(swapStyle: HtmxSwapStyle, target: Node, fragment: Node, settleInfo: HtmxSettleInfo) => boolean|Node[]} handleSwap
 * @property {(xhr: XMLHttpRequest, parameters: FormData, elt: Node) => *|string|null} encodeParameters
 * @property {() => string[]|null} getSelectors
 */



//// UMD insanity
//// This code sets up support for (in order) AMD, ES6 modules, and globals.
//(function (root, factory) {
//    //@ts-ignore
//    if (typeof define === 'function' && define.amd) {
//        // AMD. Register as an anonymous module.
//        //@ts-ignore
//        define([], factory);
//    } else if (typeof module === 'object' && module.exports) {
//        // Node. Does not work with strict CommonJS, but
//        // only CommonJS-like environments that support module.exports,
//        // like Node.
//        module.exports = factory();
//    } else {
//        // Browser globals
//        root.htmx = root.htmx || factory();
//    }
//}(typeof self !== 'undefined' ? self : this, function () {
//return (function () {
//        'use strict';
//
//        // Public API
//        //** @type {import("./htmx").HtmxApi} */
//        // TODO: list all methods in public API
//        var htmx = {
//            onLoad: onLoadHelper,
//            process: processNode,
//            on: addEventListenerImpl,
//            off: removeEventListenerImpl,
//            trigger : triggerEvent,
//            ajax : ajaxHelper,
//            find : find,
//            findAll : findAll,
//            closest : closest,
//            values : function(elt, type){
//                var inputValues = getInputValues(elt, type || "post");
//                return inputValues.values;
//            },
//            remove : removeElement,
//            addClass : addClassToElement,
//            removeClass : removeClassFromElement,
//            toggleClass : toggleClassOnElement,
//            takeClass : takeClassForElement,
//            defineExtension : defineExtension,
//            removeExtension : removeExtension,
//            logAll : logAll,
//            logger : null,
//            config : {
//                historyEnabled:true,
//                historyCacheSize:10,
//                refreshOnHistoryMiss:false,
//                defaultSwapStyle:'innerHTML',
//                defaultSwapDelay:0,
//                defaultSettleDelay:20,
//                includeIndicatorStyles:true,
//                indicatorClass:'htmx-indicator',
//                requestClass:'htmx-request',
//                addedClass:'htmx-added',
//                settlingClass:'htmx-settling',
//                swappingClass:'htmx-swapping',
//                allowEval:true,
//                inlineScriptNonce:'',
//                attributesToSettle:["class", "style", "width", "height"],
//                withCredentials:false,
//                timeout:0,
//                wsReconnectDelay: 'full-jitter',
//                wsBinaryType: 'blob',
//                disableSelector: "[hx-disable], [data-hx-disable]",
//                useTemplateFragments: false,
//                scrollBehavior: 'smooth',
//                defaultFocusScroll: false,
//                getCacheBusterParam: false,
//                globalViewTransitions: false,
//            },
//            parseInterval:parseInterval,
//            _:internalEval,
//            createEventSource: function(url){
//                return new EventSource(url, {withCredentials:true})
//            },
//            createWebSocket: function(url){
//                var sock = new WebSocket(url, []);
//                sock.binaryType = htmx.config.wsBinaryType;
//                return sock;
//            },
//            version: "1.9.2"
//        };
//
//        /** @type {import("./htmx").HtmxInternalApi} */
//        var internalAPI = {
//            addTriggerHandler: addTriggerHandler,
//            bodyContains: bodyContains,
//            canAccessLocalStorage: canAccessLocalStorage,
//            filterValues: filterValues,
//            hasAttribute: hasAttribute,
//            getAttributeValue: getAttributeValue,
//            getClosestMatch: getClosestMatch,
//            getExpressionVars: getExpressionVars,
//            getHeaders: getHeaders,
//            getInputValues: getInputValues,
//            getInternalData: getInternalData,
//            getSwapSpecification: getSwapSpecification,
//            getTriggerSpecs: getTriggerSpecs,
//            getTarget: getTarget,
//            makeFragment: makeFragment,
//            mergeObjects: mergeObjects,
//            makeSettleInfo: makeSettleInfo,
//            oobSwap: oobSwap,
//            selectAndSwap: selectAndSwap,
//            settleImmediately: settleImmediately,
//            shouldCancel: shouldCancel,
//            triggerEvent: triggerEvent,
//            triggerErrorEvent: triggerErrorEvent,
//            withExtensions: withExtensions,
//        }
//
//        var VERBS = ['get', 'post', 'put', 'delete', 'patch'];
//        var VERB_SELECTOR = VERBS.map(function(verb){
//            return "[hx-" + verb + "], [data-hx-" + verb + "]"
//        }).join(", ");
//
//        //====================================================================
//        // Utilities
//        //====================================================================
//
//        function parseInterval(str) {
//            if (str == undefined)  {
//                return undefined
//            }
//            if (str.slice(-2) == "ms") {
//                return parseFloat(str.slice(0,-2)) || undefined
//            }
//            if (str.slice(-1) == "s") {
//                return (parseFloat(str.slice(0,-1)) * 1000) || undefined
//            }
//            if (str.slice(-1) == "m") {
//                return (parseFloat(str.slice(0,-1)) * 1000 * 60) || undefined
//            }
//            return parseFloat(str) || undefined
//        }
//
//        /**
//         * @param {HTMLElement} elt
//         * @param {string} name
//         * @returns {(string | null)}
//         */
//        function getRawAttribute(elt, name) {
//            return elt.getAttribute && elt.getAttribute(name);
//        }
//
//        // resolve with both hx and data-hx prefixes
//        function hasAttribute(elt, qualifiedName) {
//            return elt.hasAttribute && (elt.hasAttribute(qualifiedName) ||
//                elt.hasAttribute("data-" + qualifiedName));
//        }
//
//        /**
//         *
//         * @param {HTMLElement} elt
//         * @param {string} qualifiedName
//         * @returns {(string | null)}
//         */
//        function getAttributeValue(elt, qualifiedName) {
//            return getRawAttribute(elt, qualifiedName) || getRawAttribute(elt, "data-" + qualifiedName);
//        }
//
//        /**
//         * @param {HTMLElement} elt
//         * @returns {HTMLElement | null}
//         */
//        function parentElt(elt) {
//            return elt.parentElement;
//        }
//
//        /**
//         * @returns {Document}
//         */
//        function getDocument() {
//            return document;
//        }
//
//        /**
//         * @param {HTMLElement} elt
//         * @param {(e:HTMLElement) => boolean} condition
//         * @returns {HTMLElement | null}
//         */
//        function getClosestMatch(elt, condition) {
//            while (elt && !condition(elt)) {
//                elt = parentElt(elt);
//            }
//
//            return elt ? elt : null;
//        }
//
//        function getAttributeValueWithDisinheritance(initialElement, ancestor, attributeName){
//            var attributeValue = getAttributeValue(ancestor, attributeName);
//            var disinherit = getAttributeValue(ancestor, "hx-disinherit");
//            if (initialElement !== ancestor && disinherit && (disinherit === "*" || disinherit.split(" ").indexOf(attributeName) >= 0)) {
//                return "unset";
//            } else {
//                return attributeValue
//            }
//        }
//
//        /**
//         * @param {HTMLElement} elt
//         * @param {string} attributeName
//         * @returns {string | null}
//         */
//        function getClosestAttributeValue(elt, attributeName) {
//            var closestAttr = null;
//            getClosestMatch(elt, function (e) {
//                return closestAttr = getAttributeValueWithDisinheritance(elt, e, attributeName);
//            });
//            if (closestAttr !== "unset") {
//                return closestAttr;
//            }
//        }
//
//        /**
//         * @param {HTMLElement} elt
//         * @param {string} selector
//         * @returns {boolean}
//         */
//        function matches(elt, selector) {
//            // @ts-ignore: non-standard properties for browser compatability
//            // noinspection JSUnresolvedVariable
//            var matchesFunction = elt.matches || elt.matchesSelector || elt.msMatchesSelector || elt.mozMatchesSelector || elt.webkitMatchesSelector || elt.oMatchesSelector;
//            return matchesFunction && matchesFunction.call(elt, selector);
//        }
//
//        /**
//         * @param {string} str
//         * @returns {string}
//         */
//        function getStartTag(str) {
//            var tagMatcher = /<([a-z][^\/\0>\x20\t\r\n\f]*)/i
//            var match = tagMatcher.exec( str );
//            if (match) {
//                return match[1].toLowerCase();
//            } else {
//                return "";
//            }
//        }
//
//        /**
//         *
//         * @param {string} resp
//         * @param {number} depth
//         * @returns {Element}
//         */
//        function parseHTML(resp, depth) {
//            var parser = new DOMParser();
//            var responseDoc = parser.parseFromString(resp, "text/html");
//
//            /** @type {Element} */
//            var responseNode = responseDoc.body;
//            while (depth > 0) {
//                depth--;
//                // @ts-ignore
//                responseNode = responseNode.firstChild;
//            }
//            if (responseNode == null) {
//                // @ts-ignore
//                responseNode = getDocument().createDocumentFragment();
//            }
//            return responseNode;
//        }
//
//        function aFullPageResponse(resp) {
//            return resp.match(/<body/);
//        }
//
//        /**
//         *
//         * @param {string} resp
//         * @returns {Element}
//         */
//        function makeFragment(resp) {
//            var partialResponse = !aFullPageResponse(resp);
//            if (htmx.config.useTemplateFragments && partialResponse) {
//                var documentFragment = parseHTML("<body><template>" + resp + "</template></body>", 0);
//                // @ts-ignore type mismatch between DocumentFragment and Element.
//                // TODO: Are these close enough for htmx to use interchangably?
//                return documentFragment.querySelector('template').content;
//            } else {
//                var startTag = getStartTag(resp);
//                switch (startTag) {
//                    case "thead":
//                    case "tbody":
//                    case "tfoot":
//                    case "colgroup":
//                    case "caption":
//                        return parseHTML("<table>" + resp + "</table>", 1);
//                    case "col":
//                        return parseHTML("<table><colgroup>" + resp + "</colgroup></table>", 2);
//                    case "tr":
//                        return parseHTML("<table><tbody>" + resp + "</tbody></table>", 2);
//                    case "td":
//                    case "th":
//                        return parseHTML("<table><tbody><tr>" + resp + "</tr></tbody></table>", 3);
//                    case "script":
//                        return parseHTML("<div>" + resp + "</div>", 1);
//                    default:
//                        return parseHTML(resp, 0);
//                }
//            }
//        }
//
//        /**
//         * @param {Function} func
//         */
//        function maybeCall(func){
//            if(func) {
//                func();
//            }
//        }
//
//        /**
//         * @param {any} o
//         * @param {string} type
//         * @returns
//         */
//        function isType(o, type) {
//            return Object.prototype.toString.call(o) === "[object " + type + "]";
//        }
//
//        /**
//         * @param {*} o
//         * @returns {o is Function}
//         */
//        function isFunction(o) {
//            return isType(o, "Function");
//        }
//
//        /**
//         * @param {*} o
//         * @returns {o is Object}
//         */
//        function isRawObject(o) {
//            return isType(o, "Object");
//        }
//
//        /**
//         * getInternalData retrieves "private" data stored by htmx within an element
//         * @param {HTMLElement} elt
//         * @returns {*}
//         */
//        function getInternalData(elt) {
//            var dataProp = 'htmx-internal-data';
//            var data = elt[dataProp];
//            if (!data) {
//                data = elt[dataProp] = {};
//            }
//            return data;
//        }
//
//        /**
//         * toArray converts an ArrayLike object into a real array.
//         * @param {ArrayLike} arr
//         * @returns {any[]}
//         */
//        function toArray(arr) {
//            var returnArr = [];
//            if (arr) {
//                for (var i = 0; i < arr.length; i++) {
//                    returnArr.push(arr[i]);
//                }
//            }
//            return returnArr
//        }
//
//        function forEach(arr, func) {
//            if (arr) {
//                for (var i = 0; i < arr.length; i++) {
//                    func(arr[i]);
//                }
//            }
//        }
//
//        function isScrolledIntoView(el) {
//            var rect = el.getBoundingClientRect();
//            var elemTop = rect.top;
//            var elemBottom = rect.bottom;
//            return elemTop < window.innerHeight && elemBottom >= 0;
//        }
//
//        function bodyContains(elt) {
//            // IE Fix
//            if (elt.getRootNode && elt.getRootNode() instanceof ShadowRoot) {
//                return getDocument().body.contains(elt.getRootNode().host);
//            } else {
//                return getDocument().body.contains(elt);
//            }
//        }
//
//        function splitOnWhitespace(trigger) {
//            return trigger.trim().split(/\s+/);
//        }
//
//        /**
//         * mergeObjects takes all of the keys from
//         * obj2 and duplicates them into obj1
//         * @param {Object} obj1
//         * @param {Object} obj2
//         * @returns {Object}
//         */
//        function mergeObjects(obj1, obj2) {
//            for (var key in obj2) {
//                if (obj2.hasOwnProperty(key)) {
//                    obj1[key] = obj2[key];
//                }
//            }
//            return obj1;
//        }
//
//        function parseJSON(jString) {
//            try {
//                return JSON.parse(jString);
//            } catch(error) {
//                logError(error);
//                return null;
//            }
//        }
//
//        function canAccessLocalStorage() {
//            var test = 'htmx:localStorageTest';
//            try {
//                localStorage.setItem(test, test);
//                localStorage.removeItem(test);
//                return true;
//            } catch(e) {
//                return false;
//            }
//        }
//
//        function normalizePath(path) {
//            try {
//                var url = new URL(path);
//                if (url) {
//                    path = url.pathname + url.search;
//                }
//                // remove trailing slash, unless index page
//                if (!path.match('^/$')) {
//                    path = path.replace(/\/+$/, '');
//                }
//                return path;
//            } catch (e) {
//                // be kind to IE11, which doesn't support URL()
//                return path;
//            }
//        }
//
//        //==========================================================================================
//        // public API
//        //==========================================================================================
//
//        function internalEval(str){
//            return maybeEval(getDocument().body, function () {
//                return eval(str);
//            });
//        }
//
//        function onLoadHelper(callback) {
//            var value = htmx.on("htmx:load", function(evt) {
//                callback(evt.detail.elt);
//            });
//            return value;
//        }
//
//        function logAll(){
//            htmx.logger = function(elt, event, data) {
//                if(console) {
//                    console.log(event, elt, data);
//                }
//            }
//        }
//
//        function find(eltOrSelector, selector) {
//            if (selector) {
//                return eltOrSelector.querySelector(selector);
//            } else {
//                return find(getDocument(), eltOrSelector);
//            }
//        }
//
//        function findAll(eltOrSelector, selector) {
//            if (selector) {
//                return eltOrSelector.querySelectorAll(selector);
//            } else {
//                return findAll(getDocument(), eltOrSelector);
//            }
//        }
//
//        function removeElement(elt, delay) {
//            elt = resolveTarget(elt);
//            if (delay) {
//                setTimeout(function(){
//                    removeElement(elt);
//                    elt = null;
//                }, delay);
//            } else {
//                elt.parentElement.removeChild(elt);
//            }
//        }
//
//        function addClassToElement(elt, clazz, delay) {
//            elt = resolveTarget(elt);
//            if (delay) {
//                setTimeout(function(){
//                    addClassToElement(elt, clazz);
//                    elt = null;
//                }, delay);
//            } else {
//                elt.classList && elt.classList.add(clazz);
//            }
//        }
//
//        function removeClassFromElement(elt, clazz, delay) {
//            elt = resolveTarget(elt);
//            if (delay) {
//                setTimeout(function(){
//                    removeClassFromElement(elt, clazz);
//                    elt = null;
//                }, delay);
//            } else {
//                if (elt.classList) {
//                    elt.classList.remove(clazz);
//                    // if there are no classes left, remove the class attribute
//                    if (elt.classList.length === 0) {
//                        elt.removeAttribute("class");
//                    }
//                }
//            }
//        }
//
//        function toggleClassOnElement(elt, clazz) {
//            elt = resolveTarget(elt);
//            elt.classList.toggle(clazz);
//        }
//
//        function takeClassForElement(elt, clazz) {
//            elt = resolveTarget(elt);
//            forEach(elt.parentElement.children, function(child){
//                removeClassFromElement(child, clazz);
//            })
//            addClassToElement(elt, clazz);
//        }
//
//        function closest(elt, selector) {
//            elt = resolveTarget(elt);
//            if (elt.closest) {
//                return elt.closest(selector);
//            } else {
//                // TODO remove when IE goes away
//                do{
//                    if (elt == null || matches(elt, selector)){
//                        return elt;
//                    }
//                }
//                while (elt = elt && parentElt(elt));
//                return null;
//            }
//        }
//
//        function normalizeSelector(selector) {
//            var trimmedSelector = selector.trim();
//            if (trimmedSelector.startsWith("<") && trimmedSelector.endsWith("/>")) {
//                return trimmedSelector.substring(1, trimmedSelector.length - 2);
//            } else {
//                return trimmedSelector;
//            }
//        }
//
//        function querySelectorAllExt(elt, selector) {
//            if (selector.indexOf("closest ") === 0) {
//                return [closest(elt, normalizeSelector(selector.substr(8)))];
//            } else if (selector.indexOf("find ") === 0) {
//                return [find(elt, normalizeSelector(selector.substr(5)))];
//            } else if (selector.indexOf("next ") === 0) {
//                return [scanForwardQuery(elt, normalizeSelector(selector.substr(5)))];
//            } else if (selector.indexOf("previous ") === 0) {
//                return [scanBackwardsQuery(elt, normalizeSelector(selector.substr(9)))];
//            } else if (selector === 'document') {
//                return [document];
//            } else if (selector === 'window') {
//                return [window];
//            } else {
//                return getDocument().querySelectorAll(normalizeSelector(selector));
//            }
//        }
//
//        var scanForwardQuery = function(start, match) {
//            var results = getDocument().querySelectorAll(match);
//            for (var i = 0; i < results.length; i++) {
//                var elt = results[i];
//                if (elt.compareDocumentPosition(start) === Node.DOCUMENT_POSITION_PRECEDING) {
//                    return elt;
//                }
//            }
//        }
//
//        var scanBackwardsQuery = function(start, match) {
//            var results = getDocument().querySelectorAll(match);
//            for (var i = results.length - 1; i >= 0; i--) {
//                var elt = results[i];
//                if (elt.compareDocumentPosition(start) === Node.DOCUMENT_POSITION_FOLLOWING) {
//                    return elt;
//                }
//            }
//        }
//
//        function querySelectorExt(eltOrSelector, selector) {
//            if (selector) {
//                return querySelectorAllExt(eltOrSelector, selector)[0];
//            } else {
//                return querySelectorAllExt(getDocument().body, eltOrSelector)[0];
//            }
//        }
//
//        function resolveTarget(arg2) {
//            if (isType(arg2, 'String')) {
//                return find(arg2);
//            } else {
//                return arg2;
//            }
//        }
//
//        function processEventArgs(arg1, arg2, arg3) {
//            if (isFunction(arg2)) {
//                return {
//                    target: getDocument().body,
//                    event: arg1,
//                    listener: arg2
//                }
//            } else {
//                return {
//                    target: resolveTarget(arg1),
//                    event: arg2,
//                    listener: arg3
//                }
//            }
//
//        }
//
//        function addEventListenerImpl(arg1, arg2, arg3) {
//            ready(function(){
//                var eventArgs = processEventArgs(arg1, arg2, arg3);
//                eventArgs.target.addEventListener(eventArgs.event, eventArgs.listener);
//            })
//            var b = isFunction(arg2);
//            return b ? arg2 : arg3;
//        }
//
//        function removeEventListenerImpl(arg1, arg2, arg3) {
//            ready(function(){
//                var eventArgs = processEventArgs(arg1, arg2, arg3);
//                eventArgs.target.removeEventListener(eventArgs.event, eventArgs.listener);
//            })
//            return isFunction(arg2) ? arg2 : arg3;
//        }
//
//        //====================================================================
//        // Node processing
//        //====================================================================
//
//        var DUMMY_ELT = getDocument().createElement("output"); // dummy element for bad selectors
//        function findAttributeTargets(elt, attrName) {
//            var attrTarget = getClosestAttributeValue(elt, attrName);
//            if (attrTarget) {
//                if (attrTarget === "this") {
//                    return [findThisElement(elt, attrName)];
//                } else {
//                    var result = querySelectorAllExt(elt, attrTarget);
//                    if (result.length === 0) {
//                        logError('The selector "' + attrTarget + '" on ' + attrName + " returned no matches!");
//                        return [DUMMY_ELT]
//                    } else {
//                        return result;
//                    }
//                }
//            }
//        }
//
//        function findThisElement(elt, attribute){
//            return getClosestMatch(elt, function (elt) {
//                return getAttributeValue(elt, attribute) != null;
//            })
//        }
//
//        function getTarget(elt) {
//            var targetStr = getClosestAttributeValue(elt, "hx-target");
//            if (targetStr) {
//                if (targetStr === "this") {
//                    return findThisElement(elt,'hx-target');
//                } else {
//                    return querySelectorExt(elt, targetStr)
//                }
//            } else {
//                var data = getInternalData(elt);
//                if (data.boosted) {
//                    return getDocument().body;
//                } else {
//                    return elt;
//                }
//            }
//        }
//
//        function shouldSettleAttribute(name) {
//            var attributesToSettle = htmx.config.attributesToSettle;
//            for (var i = 0; i < attributesToSettle.length; i++) {
//                if (name === attributesToSettle[i]) {
//                    return true;
//                }
//            }
//            return false;
//        }
//
//        function cloneAttributes(mergeTo, mergeFrom) {
//            forEach(mergeTo.attributes, function (attr) {
//                if (!mergeFrom.hasAttribute(attr.name) && shouldSettleAttribute(attr.name)) {
//                    mergeTo.removeAttribute(attr.name)
//                }
//            });
//            forEach(mergeFrom.attributes, function (attr) {
//                if (shouldSettleAttribute(attr.name)) {
//                    mergeTo.setAttribute(attr.name, attr.value);
//                }
//            });
//        }
//
//        function isInlineSwap(swapStyle, target) {
//            var extensions = getExtensions(target);
//            for (var i = 0; i < extensions.length; i++) {
//                var extension = extensions[i];
//                try {
//                    if (extension.isInlineSwap(swapStyle)) {
//                        return true;
//                    }
//                } catch(e) {
//                    logError(e);
//                }
//            }
//            return swapStyle === "outerHTML";
//        }
//
//        /**
//         *
//         * @param {string} oobValue
//         * @param {HTMLElement} oobElement
//         * @param {*} settleInfo
//         * @returns
//         */
//        function oobSwap(oobValue, oobElement, settleInfo) {
//            var selector = "#" + oobElement.id;
//            var swapStyle = "outerHTML";
//            if (oobValue === "true") {
//                // do nothing
//            } else if (oobValue.indexOf(":") > 0) {
//                swapStyle = oobValue.substr(0, oobValue.indexOf(":"));
//                selector  = oobValue.substr(oobValue.indexOf(":") + 1, oobValue.length);
//            } else {
//                swapStyle = oobValue;
//            }
//
//            var targets = getDocument().querySelectorAll(selector);
//            if (targets) {
//                forEach(
//                    targets,
//                    function (target) {
//                        var fragment;
//                        var oobElementClone = oobElement.cloneNode(true);
//                        fragment = getDocument().createDocumentFragment();
//                        fragment.appendChild(oobElementClone);
//                        if (!isInlineSwap(swapStyle, target)) {
//                            fragment = oobElementClone; // if this is not an inline swap, we use the content of the node, not the node itself
//                        }
//
//                        var beforeSwapDetails = {shouldSwap: true, target: target, fragment:fragment };
//                        if (!triggerEvent(target, 'htmx:oobBeforeSwap', beforeSwapDetails)) return;
//
//                        target = beforeSwapDetails.target; // allow re-targeting
//                        if (beforeSwapDetails['shouldSwap']){
//                            swap(swapStyle, target, target, fragment, settleInfo);
//                        }
//                        forEach(settleInfo.elts, function (elt) {
//                            triggerEvent(elt, 'htmx:oobAfterSwap', beforeSwapDetails);
//                        });
//                    }
//                );
//                oobElement.parentNode.removeChild(oobElement);
//            } else {
//                oobElement.parentNode.removeChild(oobElement);
//                triggerErrorEvent(getDocument().body, "htmx:oobErrorNoTarget", {content: oobElement});
//            }
//            return oobValue;
//        }
//
//        function handleOutOfBandSwaps(elt, fragment, settleInfo) {
//            var oobSelects = getClosestAttributeValue(elt, "hx-select-oob");
//            if (oobSelects) {
//                var oobSelectValues = oobSelects.split(",");
//                for (let i = 0; i < oobSelectValues.length; i++) {
//                    var oobSelectValue = oobSelectValues[i].split(":", 2);
//                    var id = oobSelectValue[0].trim();
//                    if (id.indexOf("#") === 0) {
//                        id = id.substring(1);
//                    }
//                    var oobValue = oobSelectValue[1] || "true";
//                    var oobElement = fragment.querySelector("#" + id);
//                    if (oobElement) {
//                        oobSwap(oobValue, oobElement, settleInfo);
//                    }
//                }
//            }
//            forEach(findAll(fragment, '[hx-swap-oob], [data-hx-swap-oob]'), function (oobElement) {
//                var oobValue = getAttributeValue(oobElement, "hx-swap-oob");
//                if (oobValue != null) {
//                    oobSwap(oobValue, oobElement, settleInfo);
//                }
//            });
//        }
//
//        function handlePreservedElements(fragment) {
//            forEach(findAll(fragment, '[hx-preserve], [data-hx-preserve]'), function (preservedElt) {
//                var id = getAttributeValue(preservedElt, "id");
//                var oldElt = getDocument().getElementById(id);
//                if (oldElt != null) {
//                    preservedElt.parentNode.replaceChild(oldElt, preservedElt);
//                }
//            });
//        }
//
//        function handleAttributes(parentNode, fragment, settleInfo) {
//            forEach(fragment.querySelectorAll("[id]"), function (newNode) {
//                if (newNode.id && newNode.id.length > 0) {
//                    var normalizedId = newNode.id.replace("'", "\\'");
//                    var normalizedTag = newNode.tagName.replace(':', '\\:');
//                    var oldNode = parentNode.querySelector(normalizedTag + "[id='" + normalizedId + "']");
//                    if (oldNode && oldNode !== parentNode) {
//                        var newAttributes = newNode.cloneNode();
//                        cloneAttributes(newNode, oldNode);
//                        settleInfo.tasks.push(function () {
//                            cloneAttributes(newNode, newAttributes);
//                        });
//                    }
//                }
//            });
//        }
//
//        function makeAjaxLoadTask(child) {
//            return function () {
//                removeClassFromElement(child, htmx.config.addedClass);
//                processNode(child);
//                processScripts(child);
//                processFocus(child)
//                triggerEvent(child, 'htmx:load');
//            };
//        }
//
//        function processFocus(child) {
//            var autofocus = "[autofocus]";
//            var autoFocusedElt = matches(child, autofocus) ? child : child.querySelector(autofocus)
//            if (autoFocusedElt != null) {
//                autoFocusedElt.focus();
//            }
//        }
//
//        function insertNodesBefore(parentNode, insertBefore, fragment, settleInfo) {
//            handleAttributes(parentNode, fragment, settleInfo);
//            while(fragment.childNodes.length > 0){
//                var child = fragment.firstChild;
//                addClassToElement(child, htmx.config.addedClass);
//                parentNode.insertBefore(child, insertBefore);
//                if (child.nodeType !== Node.TEXT_NODE && child.nodeType !== Node.COMMENT_NODE) {
//                    settleInfo.tasks.push(makeAjaxLoadTask(child));
//                }
//            }
//        }
//
//        // based on https://gist.github.com/hyamamoto/fd435505d29ebfa3d9716fd2be8d42f0,
//        // derived from Java's string hashcode implementation
//        function stringHash(string, hash) {
//            var char = 0;
//            while (char < string.length){
//                hash = (hash << 5) - hash + string.charCodeAt(char++) | 0; // bitwise or ensures we have a 32-bit int
//            }
//            return hash;
//        }
//
//        function attributeHash(elt) {
//            var hash = 0;
//            // IE fix
//            if (elt.attributes) {
//                for (var i = 0; i < elt.attributes.length; i++) {
//                    var attribute = elt.attributes[i];
//                    if(attribute.value){ // only include attributes w/ actual values (empty is same as non-existent)
//                        hash = stringHash(attribute.name, hash);
//                        hash = stringHash(attribute.value, hash);
//                    }
//                }
//            }
//            return hash;
//        }
//
//        function deInitNode(element) {
//            var internalData = getInternalData(element);
//            if (internalData.timeout) {
//                clearTimeout(internalData.timeout);
//            }
//            if (internalData.webSocket) {
//                internalData.webSocket.close();
//            }
//            if (internalData.sseEventSource) {
//                internalData.sseEventSource.close();
//            }
//            if (internalData.listenerInfos) {
//                forEach(internalData.listenerInfos, function (info) {
//                    if (info.on) {
//                        info.on.removeEventListener(info.trigger, info.listener);
//                    }
//                });
//            }
//            if (internalData.onHandlers) {
//                for (let i = 0; i < internalData.onHandlers.length; i++) {
//                    const handlerInfo = internalData.onHandlers[i];
//                    element.removeEventListener(handlerInfo.name, handlerInfo.handler);
//                }
//            }
//        }
//
//        function cleanUpElement(element) {
//            triggerEvent(element, "htmx:beforeCleanupElement")
//            deInitNode(element);
//            if (element.children) { // IE
//                forEach(element.children, function(child) { cleanUpElement(child) });
//            }
//        }
//
//        function swapOuterHTML(target, fragment, settleInfo) {
//            if (target.tagName === "BODY") {
//                return swapInnerHTML(target, fragment, settleInfo);
//            } else {
//                // @type {HTMLElement}
//                var newElt
//                var eltBeforeNewContent = target.previousSibling;
//                insertNodesBefore(parentElt(target), target, fragment, settleInfo);
//                if (eltBeforeNewContent == null) {
//                    newElt = parentElt(target).firstChild;
//                } else {
//                    newElt = eltBeforeNewContent.nextSibling;
//                }
//                getInternalData(target).replacedWith = newElt; // tuck away so we can fire events on it later
//                settleInfo.elts = [] // clear existing elements
//                while(newElt && newElt !== target) {
//                    if (newElt.nodeType === Node.ELEMENT_NODE) {
//                        settleInfo.elts.push(newElt);
//                    }
//                    newElt = newElt.nextElementSibling;
//                }
//                cleanUpElement(target);
//                parentElt(target).removeChild(target);
//            }
//        }
//
//        function swapAfterBegin(target, fragment, settleInfo) {
//            return insertNodesBefore(target, target.firstChild, fragment, settleInfo);
//        }
//
//        function swapBeforeBegin(target, fragment, settleInfo) {
//            return insertNodesBefore(parentElt(target), target, fragment, settleInfo);
//        }
//
//        function swapBeforeEnd(target, fragment, settleInfo) {
//            return insertNodesBefore(target, null, fragment, settleInfo);
//        }
//
//        function swapAfterEnd(target, fragment, settleInfo) {
//            return insertNodesBefore(parentElt(target), target.nextSibling, fragment, settleInfo);
//        }
//        function swapDelete(target, fragment, settleInfo) {
//            cleanUpElement(target);
//            return parentElt(target).removeChild(target);
//        }
//
//        function swapInnerHTML(target, fragment, settleInfo) {
//            var firstChild = target.firstChild;
//            insertNodesBefore(target, firstChild, fragment, settleInfo);
//            if (firstChild) {
//                while (firstChild.nextSibling) {
//                    cleanUpElement(firstChild.nextSibling)
//                    target.removeChild(firstChild.nextSibling);
//                }
//                cleanUpElement(firstChild)
//                target.removeChild(firstChild);
//            }
//        }
//
//        function maybeSelectFromResponse(elt, fragment) {
//            var selector = getClosestAttributeValue(elt, "hx-select");
//            if (selector) {
//                var newFragment = getDocument().createDocumentFragment();
//                forEach(fragment.querySelectorAll(selector), function (node) {
//                    newFragment.appendChild(node);
//                });
//                fragment = newFragment;
//            }
//            return fragment;
//        }
//
//        function swap(swapStyle, elt, target, fragment, settleInfo) {
//            switch (swapStyle) {
//                case "none":
//                    return;
//                case "outerHTML":
//                    swapOuterHTML(target, fragment, settleInfo);
//                    return;
//                case "afterbegin":
//                    swapAfterBegin(target, fragment, settleInfo);
//                    return;
//                case "beforebegin":
//                    swapBeforeBegin(target, fragment, settleInfo);
//                    return;
//                case "beforeend":
//                    swapBeforeEnd(target, fragment, settleInfo);
//                    return;
//                case "afterend":
//                    swapAfterEnd(target, fragment, settleInfo);
//                    return;
//                case "delete":
//                    swapDelete(target, fragment, settleInfo);
//                    return;
//                default:
//                    var extensions = getExtensions(elt);
//                    for (var i = 0; i < extensions.length; i++) {
//                        var ext = extensions[i];
//                        try {
//                            var newElements = ext.handleSwap(swapStyle, target, fragment, settleInfo);
//                            if (newElements) {
//                                if (typeof newElements.length !== 'undefined') {
//                                    // if handleSwap returns an array (like) of elements, we handle them
//                                    for (var j = 0; j < newElements.length; j++) {
//                                        var child = newElements[j];
//                                        if (child.nodeType !== Node.TEXT_NODE && child.nodeType !== Node.COMMENT_NODE) {
//                                            settleInfo.tasks.push(makeAjaxLoadTask(child));
//                                        }
//                                    }
//                                }
//                                return;
//                            }
//                        } catch (e) {
//                            logError(e);
//                        }
//                    }
//                    if (swapStyle === "innerHTML") {
//                        swapInnerHTML(target, fragment, settleInfo);
//                    } else {
//                        swap(htmx.config.defaultSwapStyle, elt, target, fragment, settleInfo);
//                    }
//            }
//        }
//
//        function findTitle(content) {
//            if (content.indexOf('<title') > -1) {
//                var contentWithSvgsRemoved = content.replace(/<svg(\s[^>]*>|>)([\s\S]*?)<\/svg>/gim, '');
//                var result = contentWithSvgsRemoved.match(/<title(\s[^>]*>|>)([\s\S]*?)<\/title>/im);
//
//                if (result) {
//                    return result[2];
//                }
//            }
//        }
//
//        function selectAndSwap(swapStyle, target, elt, responseText, settleInfo) {
//            settleInfo.title = findTitle(responseText);
//            var fragment = makeFragment(responseText);
//            if (fragment) {
//                handleOutOfBandSwaps(elt, fragment, settleInfo);
//                fragment = maybeSelectFromResponse(elt, fragment);
//                handlePreservedElements(fragment);
//                return swap(swapStyle, elt, target, fragment, settleInfo);
//            }
//        }
//
//        function handleTrigger(xhr, header, elt) {
//            var triggerBody = xhr.getResponseHeader(header);
//            if (triggerBody.indexOf("{") === 0) {
//                var triggers = parseJSON(triggerBody);
//                for (var eventName in triggers) {
//                    if (triggers.hasOwnProperty(eventName)) {
//                        var detail = triggers[eventName];
//                        if (!isRawObject(detail)) {
//                            detail = {"value": detail}
//                        }
//                        triggerEvent(elt, eventName, detail);
//                    }
//                }
//            } else {
//                triggerEvent(elt, triggerBody, []);
//            }
//        }
//
//        var WHITESPACE = /\s/;
//        var WHITESPACE_OR_COMMA = /[\s,]/;
//        var SYMBOL_START = /[_$a-zA-Z]/;
//        var SYMBOL_CONT = /[_$a-zA-Z0-9]/;
//        var STRINGISH_START = ['"', "'", "/"];
//        var NOT_WHITESPACE = /[^\s]/;
//        function tokenizeString(str) {
//            var tokens = [];
//            var position = 0;
//            while (position < str.length) {
//                if(SYMBOL_START.exec(str.charAt(position))) {
//                    var startPosition = position;
//                    while (SYMBOL_CONT.exec(str.charAt(position + 1))) {
//                        position++;
//                    }
//                    tokens.push(str.substr(startPosition, position - startPosition + 1));
//                } else if (STRINGISH_START.indexOf(str.charAt(position)) !== -1) {
//                    var startChar = str.charAt(position);
//                    var startPosition = position;
//                    position++;
//                    while (position < str.length && str.charAt(position) !== startChar ) {
//                        if (str.charAt(position) === "\\") {
//                            position++;
//                        }
//                        position++;
//                    }
//                    tokens.push(str.substr(startPosition, position - startPosition + 1));
//                } else {
//                    var symbol = str.charAt(position);
//                    tokens.push(symbol);
//                }
//                position++;
//            }
//            return tokens;
//        }
//
//        function isPossibleRelativeReference(token, last, paramName) {
//            return SYMBOL_START.exec(token.charAt(0)) &&
//                token !== "true" &&
//                token !== "false" &&
//                token !== "this" &&
//                token !== paramName &&
//                last !== ".";
//        }
//
//        function maybeGenerateConditional(elt, tokens, paramName) {
//            if (tokens[0] === '[') {
//                tokens.shift();
//                var bracketCount = 1;
//                var conditionalSource = " return (function(" + paramName + "){ return (";
//                var last = null;
//                while (tokens.length > 0) {
//                    var token = tokens[0];
//                    if (token === "]") {
//                        bracketCount--;
//                        if (bracketCount === 0) {
//                            if (last === null) {
//                                conditionalSource = conditionalSource + "true";
//                            }
//                            tokens.shift();
//                            conditionalSource += ")})";
//                            try {
//                                var conditionFunction = maybeEval(elt,function () {
//                                    return Function(conditionalSource)();
//                                    },
//                                    function(){return true})
//                                conditionFunction.source = conditionalSource;
//                                return conditionFunction;
//                            } catch (e) {
//                                triggerErrorEvent(getDocument().body, "htmx:syntax:error", {error:e, source:conditionalSource})
//                                return null;
//                            }
//                        }
//                    } else if (token === "[") {
//                        bracketCount++;
//                    }
//                    if (isPossibleRelativeReference(token, last, paramName)) {
//                            conditionalSource += "((" + paramName + "." + token + ") ? (" + paramName + "." + token + ") : (window." + token + "))";
//                    } else {
//                        conditionalSource = conditionalSource + token;
//                    }
//                    last = tokens.shift();
//                }
//            }
//        }
//
//        function consumeUntil(tokens, match) {
//            var result = "";
//            while (tokens.length > 0 && !tokens[0].match(match)) {
//                result += tokens.shift();
//            }
//            return result;
//        }
//
//        var INPUT_SELECTOR = 'input, textarea, select';
//
//        /**
//         * @param {HTMLElement} elt
//         * @returns {import("./htmx").HtmxTriggerSpecification[]}
//         */
//        function getTriggerSpecs(elt) {
//            var explicitTrigger = getAttributeValue(elt, 'hx-trigger');
//            var triggerSpecs = [];
//            if (explicitTrigger) {
//                var tokens = tokenizeString(explicitTrigger);
//                do {
//                    consumeUntil(tokens, NOT_WHITESPACE);
//                    var initialLength = tokens.length;
//                    var trigger = consumeUntil(tokens, /[,\[\s]/);
//                    if (trigger !== "") {
//                        if (trigger === "every") {
//                            var every = {trigger: 'every'};
//                            consumeUntil(tokens, NOT_WHITESPACE);
//                            every.pollInterval = parseInterval(consumeUntil(tokens, /[,\[\s]/));
//                            consumeUntil(tokens, NOT_WHITESPACE);
//                            var eventFilter = maybeGenerateConditional(elt, tokens, "event");
//                            if (eventFilter) {
//                                every.eventFilter = eventFilter;
//                            }
//                            triggerSpecs.push(every);
//                        } else if (trigger.indexOf("sse:") === 0) {
//                            triggerSpecs.push({trigger: 'sse', sseEvent: trigger.substr(4)});
//                        } else {
//                            var triggerSpec = {trigger: trigger};
//                            var eventFilter = maybeGenerateConditional(elt, tokens, "event");
//                            if (eventFilter) {
//                                triggerSpec.eventFilter = eventFilter;
//                            }
//                            while (tokens.length > 0 && tokens[0] !== ",") {
//                                consumeUntil(tokens, NOT_WHITESPACE)
//                                var token = tokens.shift();
//                                if (token === "changed") {
//                                    triggerSpec.changed = true;
//                                } else if (token === "once") {
//                                    triggerSpec.once = true;
//                                } else if (token === "consume") {
//                                    triggerSpec.consume = true;
//                                } else if (token === "delay" && tokens[0] === ":") {
//                                    tokens.shift();
//                                    triggerSpec.delay = parseInterval(consumeUntil(tokens, WHITESPACE_OR_COMMA));
//                                } else if (token === "from" && tokens[0] === ":") {
//                                    tokens.shift();
//                                    var from_arg = consumeUntil(tokens, WHITESPACE_OR_COMMA);
//                                    if (from_arg === "closest" || from_arg === "find" || from_arg === "next" || from_arg === "previous") {
//                                        tokens.shift();
//                                        from_arg +=
//                                            " " +
//                                            consumeUntil(
//                                                tokens,
//                                                WHITESPACE_OR_COMMA
//                                            );
//                                    }
//                                    triggerSpec.from = from_arg;
//                                } else if (token === "target" && tokens[0] === ":") {
//                                    tokens.shift();
//                                    triggerSpec.target = consumeUntil(tokens, WHITESPACE_OR_COMMA);
//                                } else if (token === "throttle" && tokens[0] === ":") {
//                                    tokens.shift();
//                                    triggerSpec.throttle = parseInterval(consumeUntil(tokens, WHITESPACE_OR_COMMA));
//                                } else if (token === "queue" && tokens[0] === ":") {
//                                    tokens.shift();
//                                    triggerSpec.queue = consumeUntil(tokens, WHITESPACE_OR_COMMA);
//                                } else if ((token === "root" || token === "threshold") && tokens[0] === ":") {
//                                    tokens.shift();
//                                    triggerSpec[token] = consumeUntil(tokens, WHITESPACE_OR_COMMA);
//                                } else {
//                                    triggerErrorEvent(elt, "htmx:syntax:error", {token:tokens.shift()});
//                                }
//                            }
//                            triggerSpecs.push(triggerSpec);
//                        }
//                    }
//                    if (tokens.length === initialLength) {
//                        triggerErrorEvent(elt, "htmx:syntax:error", {token:tokens.shift()});
//                    }
//                    consumeUntil(tokens, NOT_WHITESPACE);
//                } while (tokens[0] === "," && tokens.shift())
//            }
//
//            if (triggerSpecs.length > 0) {
//                return triggerSpecs;
//            } else if (matches(elt, 'form')) {
//                return [{trigger: 'submit'}];
//            } else if (matches(elt, 'input[type="button"]')){
//                return [{trigger: 'click'}];
//            } else if (matches(elt, INPUT_SELECTOR)) {
//                return [{trigger: 'change'}];
//            } else {
//                return [{trigger: 'click'}];
//            }
//        }
//
//        function cancelPolling(elt) {
//            getInternalData(elt).cancelled = true;
//        }
//
//        function processPolling(elt, handler, spec) {
//            var nodeData = getInternalData(elt);
//            nodeData.timeout = setTimeout(function () {
//                if (bodyContains(elt) && nodeData.cancelled !== true) {
//                    if (!maybeFilterEvent(spec, makeEvent('hx:poll:trigger', {triggerSpec:spec, target:elt}))) {
//                        handler(elt);
//                    }
//                    processPolling(elt, handler, spec);
//                }
//            }, spec.pollInterval);
//        }
//
//        function isLocalLink(elt) {
//            return location.hostname === elt.hostname &&
//                getRawAttribute(elt,'href') &&
//                getRawAttribute(elt,'href').indexOf("#") !== 0;
//        }
//
//        function boostElement(elt, nodeData, triggerSpecs) {
//            if ((elt.tagName === "A" && isLocalLink(elt) && (elt.target === "" || elt.target === "_self")) || elt.tagName === "FORM") {
//                nodeData.boosted = true;
//                var verb, path;
//                if (elt.tagName === "A") {
//                    verb = "get";
//                    path = elt.href; // DOM property gives the fully resolved href of a relative link
//                } else {
//                    var rawAttribute = getRawAttribute(elt, "method");
//                    verb = rawAttribute ? rawAttribute.toLowerCase() : "get";
//                    if (verb === "get") {
//                    }
//                    path = getRawAttribute(elt, 'action');
//                }
//                triggerSpecs.forEach(function(triggerSpec) {
//                    addEventListener(elt, function(elt, evt) {
//                        issueAjaxRequest(verb, path, elt, evt)
//                    }, nodeData, triggerSpec, true);
//                });
//            }
//        }
//
//        /**
//         *
//         * @param {Event} evt
//         * @param {HTMLElement} elt
//         * @returns
//         */
//        function shouldCancel(evt, elt) {
//            if (evt.type === "submit" || evt.type === "click") {
//                if (elt.tagName === "FORM") {
//                    return true;
//                }
//                if (matches(elt, 'input[type="submit"], button') && closest(elt, 'form') !== null) {
//                    return true;
//                }
//                if (elt.tagName === "A" && elt.href &&
//                    (elt.getAttribute('href') === '#' || elt.getAttribute('href').indexOf("#") !== 0)) {
//                    return true;
//                }
//            }
//            return false;
//        }
//
//        function ignoreBoostedAnchorCtrlClick(elt, evt) {
//            return getInternalData(elt).boosted && elt.tagName === "A" && evt.type === "click" && (evt.ctrlKey || evt.metaKey);
//        }
//
//        function maybeFilterEvent(triggerSpec, evt) {
//            var eventFilter = triggerSpec.eventFilter;
//            if(eventFilter){
//                try {
//                    return eventFilter(evt) !== true;
//                } catch(e) {
//                    triggerErrorEvent(getDocument().body, "htmx:eventFilter:error", {error: e, source:eventFilter.source});
//                    return true;
//                }
//            }
//            return false;
//        }
//
//        function addEventListener(elt, handler, nodeData, triggerSpec, explicitCancel) {
//            var elementData = getInternalData(elt);
//            var eltsToListenOn;
//            if (triggerSpec.from) {
//                eltsToListenOn = querySelectorAllExt(elt, triggerSpec.from);
//            } else {
//                eltsToListenOn = [elt];
//            }
//            // store the initial value of the element so we can tell if it changes
//            if (triggerSpec.changed) {
//                elementData.lastValue = elt.value;
//            }
//            forEach(eltsToListenOn, function (eltToListenOn) {
//                var eventListener = function (evt) {
//                    if (!bodyContains(elt)) {
//                        eltToListenOn.removeEventListener(triggerSpec.trigger, eventListener);
//                        return;
//                    }
//                    if (ignoreBoostedAnchorCtrlClick(elt, evt)) {
//                        return;
//                    }
//                    if (explicitCancel || shouldCancel(evt, elt)) {
//                        evt.preventDefault();
//                    }
//                    if (maybeFilterEvent(triggerSpec, evt)) {
//                        return;
//                    }
//                    var eventData = getInternalData(evt);
//                    eventData.triggerSpec = triggerSpec;
//                    if (eventData.handledFor == null) {
//                        eventData.handledFor = [];
//                    }
//                    if (eventData.handledFor.indexOf(elt) < 0) {
//                        eventData.handledFor.push(elt);
//                        if (triggerSpec.consume) {
//                            evt.stopPropagation();
//                        }
//                        if (triggerSpec.target && evt.target) {
//                            if (!matches(evt.target, triggerSpec.target)) {
//                                return;
//                            }
//                        }
//                        if (triggerSpec.once) {
//                            if (elementData.triggeredOnce) {
//                                return;
//                            } else {
//                                elementData.triggeredOnce = true;
//                            }
//                        }
//                        if (triggerSpec.changed) {
//                            if (elementData.lastValue === elt.value) {
//                                return;
//                            } else {
//                                elementData.lastValue = elt.value;
//                            }
//                        }
//                        if (elementData.delayed) {
//                            clearTimeout(elementData.delayed);
//                        }
//                        if (elementData.throttle) {
//                            return;
//                        }
//
//                        if (triggerSpec.throttle) {
//                            if (!elementData.throttle) {
//                                handler(elt, evt);
//                                elementData.throttle = setTimeout(function () {
//                                    elementData.throttle = null;
//                                }, triggerSpec.throttle);
//                            }
//                        } else if (triggerSpec.delay) {
//                            elementData.delayed = setTimeout(function() { handler(elt, evt) }, triggerSpec.delay);
//                        } else {
//                            triggerEvent(elt, 'htmx:trigger')
//                            handler(elt, evt);
//                        }
//                    }
//                };
//                if (nodeData.listenerInfos == null) {
//                    nodeData.listenerInfos = [];
//                }
//                nodeData.listenerInfos.push({
//                    trigger: triggerSpec.trigger,
//                    listener: eventListener,
//                    on: eltToListenOn
//                })
//                eltToListenOn.addEventListener(triggerSpec.trigger, eventListener);
//            });
//        }
//
//        var windowIsScrolling = false // used by initScrollHandler
//        var scrollHandler = null;
//        function initScrollHandler() {
//            if (!scrollHandler) {
//                scrollHandler = function() {
//                    windowIsScrolling = true
//                };
//                window.addEventListener("scroll", scrollHandler)
//                setInterval(function() {
//                    if (windowIsScrolling) {
//                        windowIsScrolling = false;
//                        forEach(getDocument().querySelectorAll("[hx-trigger='revealed'],[data-hx-trigger='revealed']"), function (elt) {
//                            maybeReveal(elt);
//                        })
//                    }
//                }, 200);
//            }
//        }
//
//        function maybeReveal(elt) {
//            if (!hasAttribute(elt,'data-hx-revealed') && isScrolledIntoView(elt)) {
//                elt.setAttribute('data-hx-revealed', 'true');
//                var nodeData = getInternalData(elt);
//                if (nodeData.initHash) {
//                    triggerEvent(elt, 'revealed');
//                } else {
//                    // if the node isn't initialized, wait for it before triggering the request
//                    elt.addEventListener("htmx:afterProcessNode", function(evt) { triggerEvent(elt, 'revealed') }, {once: true});
//                }
//            }
//        }
//
//        //====================================================================
//        // Web Sockets
//        //====================================================================
//
//        function processWebSocketInfo(elt, nodeData, info) {
//            var values = splitOnWhitespace(info);
//            for (var i = 0; i < values.length; i++) {
//                var value = values[i].split(/:(.+)/);
//                if (value[0] === "connect") {
//                    ensureWebSocket(elt, value[1], 0);
//                }
//                if (value[0] === "send") {
//                    processWebSocketSend(elt);
//                }
//            }
//        }
//
//        function ensureWebSocket(elt, wssSource, retryCount) {
//            if (!bodyContains(elt)) {
//                return;  // stop ensuring websocket connection when socket bearing element ceases to exist
//            }
//
//            if (wssSource.indexOf("/") == 0) {  // complete absolute paths only
//                var base_part = location.hostname + (location.port ? ':'+location.port: '');
//                if (location.protocol == 'https:') {
//                    wssSource = "wss://" + base_part + wssSource;
//                } else if (location.protocol == 'http:') {
//                    wssSource = "ws://" + base_part + wssSource;
//                }
//            }
//            var socket = htmx.createWebSocket(wssSource);
//            socket.onerror = function (e) {
//                triggerErrorEvent(elt, "htmx:wsError", {error:e, socket:socket});
//                maybeCloseWebSocketSource(elt);
//            };
//
//            socket.onclose = function (e) {
//                if ([1006, 1012, 1013].indexOf(e.code) >= 0) {  // Abnormal Closure/Service Restart/Try Again Later
//                    var delay = getWebSocketReconnectDelay(retryCount);
//                    setTimeout(function() {
//                        ensureWebSocket(elt, wssSource, retryCount+1);  // creates a websocket with a new timeout
//                    }, delay);
//                }
//            };
//            socket.onopen = function (e) {
//                retryCount = 0;
//            }
//
//            getInternalData(elt).webSocket = socket;
//            socket.addEventListener('message', function (event) {
//                if (maybeCloseWebSocketSource(elt)) {
//                    return;
//                }
//
//                var response = event.data;
//                withExtensions(elt, function(extension){
//                    response = extension.transformResponse(response, null, elt);
//                });
//
//                var settleInfo = makeSettleInfo(elt);
//                var fragment = makeFragment(response);
//                var children = toArray(fragment.children);
//                for (var i = 0; i < children.length; i++) {
//                    var child = children[i];
//                    oobSwap(getAttributeValue(child, "hx-swap-oob") || "true", child, settleInfo);
//                }
//
//                settleImmediately(settleInfo.tasks);
//            });
//        }
//
//        function maybeCloseWebSocketSource(elt) {
//            if (!bodyContains(elt)) {
//                getInternalData(elt).webSocket.close();
//                return true;
//            }
//        }
//
//        function processWebSocketSend(elt) {
//            var webSocketSourceElt = getClosestMatch(elt, function (parent) {
//                return getInternalData(parent).webSocket != null;
//            });
//            if (webSocketSourceElt) {
//                elt.addEventListener(getTriggerSpecs(elt)[0].trigger, function (evt) {
//                    var webSocket = getInternalData(webSocketSourceElt).webSocket;
//                    var headers = getHeaders(elt, webSocketSourceElt);
//                    var results = getInputValues(elt, 'post');
//                    var errors = results.errors;
//                    var rawParameters = results.values;
//                    var expressionVars = getExpressionVars(elt);
//                    var allParameters = mergeObjects(rawParameters, expressionVars);
//                    var filteredParameters = filterValues(allParameters, elt);
//                    filteredParameters['HEADERS'] = headers;
//                    if (errors && errors.length > 0) {
//                        triggerEvent(elt, 'htmx:validation:halted', errors);
//                        return;
//                    }
//                    webSocket.send(JSON.stringify(filteredParameters));
//                    if(shouldCancel(evt, elt)){
//                        evt.preventDefault();
//                    }
//                });
//            } else {
//                triggerErrorEvent(elt, "htmx:noWebSocketSourceError");
//            }
//        }
//
//        function getWebSocketReconnectDelay(retryCount) {
//            var delay = htmx.config.wsReconnectDelay;
//            if (typeof delay === 'function') {
//                // @ts-ignore
//                return delay(retryCount);
//            }
//            if (delay === 'full-jitter') {
//                var exp = Math.min(retryCount, 6);
//                var maxDelay = 1000 * Math.pow(2, exp);
//                return maxDelay * Math.random();
//            }
//            logError('htmx.config.wsReconnectDelay must either be a function or the string "full-jitter"');
//        }
//
//        //====================================================================
//        // Server Sent Events
//        //====================================================================
//
//        function processSSEInfo(elt, nodeData, info) {
//            var values = splitOnWhitespace(info);
//            for (var i = 0; i < values.length; i++) {
//                var value = values[i].split(/:(.+)/);
//                if (value[0] === "connect") {
//                    processSSESource(elt, value[1]);
//                }
//
//                if ((value[0] === "swap")) {
//                    processSSESwap(elt, value[1])
//                }
//            }
//        }
//
//        function processSSESource(elt, sseSrc) {
//            var source = htmx.createEventSource(sseSrc);
//            source.onerror = function (e) {
//                triggerErrorEvent(elt, "htmx:sseError", {error:e, source:source});
//                maybeCloseSSESource(elt);
//            };
//            getInternalData(elt).sseEventSource = source;
//        }
//
//        function processSSESwap(elt, sseEventName) {
//            var sseSourceElt = getClosestMatch(elt, hasEventSource);
//            if (sseSourceElt) {
//                var sseEventSource = getInternalData(sseSourceElt).sseEventSource;
//                var sseListener = function (event) {
//                    if (maybeCloseSSESource(sseSourceElt)) {
//                        sseEventSource.removeEventListener(sseEventName, sseListener);
//                        return;
//                    }
//
//                    ///////////////////////////
//                    // TODO: merge this code with AJAX and WebSockets code in the future.
//
//                    var response = event.data;
//                    withExtensions(elt, function(extension){
//                        response = extension.transformResponse(response, null, elt);
//                    });
//
//                    var swapSpec = getSwapSpecification(elt)
//                    var target = getTarget(elt)
//                    var settleInfo = makeSettleInfo(elt);
//
//                    selectAndSwap(swapSpec.swapStyle, elt, target, response, settleInfo)
//                    settleImmediately(settleInfo.tasks)
//                    triggerEvent(elt, "htmx:sseMessage", event)
//                };
//
//                getInternalData(elt).sseListener = sseListener;
//                sseEventSource.addEventListener(sseEventName, sseListener);
//            } else {
//                triggerErrorEvent(elt, "htmx:noSSESourceError");
//            }
//        }
//
//        function processSSETrigger(elt, handler, sseEventName) {
//            var sseSourceElt = getClosestMatch(elt, hasEventSource);
//            if (sseSourceElt) {
//                var sseEventSource = getInternalData(sseSourceElt).sseEventSource;
//                var sseListener = function () {
//                    if (!maybeCloseSSESource(sseSourceElt)) {
//                        if (bodyContains(elt)) {
//                            handler(elt);
//                        } else {
//                            sseEventSource.removeEventListener(sseEventName, sseListener);
//                        }
//                    }
//                };
//                getInternalData(elt).sseListener = sseListener;
//                sseEventSource.addEventListener(sseEventName, sseListener);
//            } else {
//                triggerErrorEvent(elt, "htmx:noSSESourceError");
//            }
//        }
//
//        function maybeCloseSSESource(elt) {
//            if (!bodyContains(elt)) {
//                getInternalData(elt).sseEventSource.close();
//                return true;
//            }
//        }
//
//        function hasEventSource(node) {
//            return getInternalData(node).sseEventSource != null;
//        }
//
//        //====================================================================
//
//        function loadImmediately(elt, handler, nodeData, delay) {
//            var load = function(){
//                if (!nodeData.loaded) {
//                    nodeData.loaded = true;
//                    handler(elt);
//                }
//            }
//            if (delay) {
//                setTimeout(load, delay);
//            } else {
//                load();
//            }
//        }
//
//        function processVerbs(elt, nodeData, triggerSpecs) {
//            var explicitAction = false;
//            forEach(VERBS, function (verb) {
//                if (hasAttribute(elt,'hx-' + verb)) {
//                    var path = getAttributeValue(elt, 'hx-' + verb);
//                    explicitAction = true;
//                    nodeData.path = path;
//                    nodeData.verb = verb;
//                    triggerSpecs.forEach(function(triggerSpec) {
//                        addTriggerHandler(elt, triggerSpec, nodeData, function (elt, evt) {
//                            issueAjaxRequest(verb, path, elt, evt)
//                        })
//                    });
//                }
//            });
//            return explicitAction;
//        }
//
//        function addTriggerHandler(elt, triggerSpec, nodeData, handler) {
//            if (triggerSpec.sseEvent) {
//                processSSETrigger(elt, handler, triggerSpec.sseEvent);
//            } else if (triggerSpec.trigger === "revealed") {
//                initScrollHandler();
//                addEventListener(elt, handler, nodeData, triggerSpec);
//                maybeReveal(elt);
//            } else if (triggerSpec.trigger === "intersect") {
//                var observerOptions = {};
//                if (triggerSpec.root) {
//                    observerOptions.root = querySelectorExt(elt, triggerSpec.root)
//                }
//                if (triggerSpec.threshold) {
//                    observerOptions.threshold = parseFloat(triggerSpec.threshold);
//                }
//                var observer = new IntersectionObserver(function (entries) {
//                    for (var i = 0; i < entries.length; i++) {
//                        var entry = entries[i];
//                        if (entry.isIntersecting) {
//                            triggerEvent(elt, "intersect");
//                            break;
//                        }
//                    }
//                }, observerOptions);
//                observer.observe(elt);
//                addEventListener(elt, handler, nodeData, triggerSpec);
//            } else if (triggerSpec.trigger === "load") {
//                if (!maybeFilterEvent(triggerSpec, makeEvent("load", {elt:elt}))) {
//                                loadImmediately(elt, handler, nodeData, triggerSpec.delay);
//                            }
//            } else if (triggerSpec.pollInterval) {
//                nodeData.polling = true;
//                processPolling(elt, handler, triggerSpec);
//            } else {
//                addEventListener(elt, handler, nodeData, triggerSpec);
//            }
//        }
//
//        function evalScript(script) {
//            if (script.type === "text/javascript" || script.type === "module" || script.type === "") {
//                var newScript = getDocument().createElement("script");
//                forEach(script.attributes, function (attr) {
//                    newScript.setAttribute(attr.name, attr.value);
//                });
//                newScript.textContent = script.textContent;
//                newScript.async = false;
//                if (htmx.config.inlineScriptNonce) {
//                    newScript.nonce = htmx.config.inlineScriptNonce;
//                }
//                var parent = script.parentElement;
//
//                try {
//                    parent.insertBefore(newScript, script);
//                } catch (e) {
//                    logError(e);
//                } finally {
//                    // remove old script element, but only if it is still in DOM
//                    if (script.parentElement) {
//                        script.parentElement.removeChild(script);
//                    }
//                }
//            }
//        }
//
//        function processScripts(elt) {
//            if (matches(elt, "script")) {
//                evalScript(elt);
//            }
//            forEach(findAll(elt, "script"), function (script) {
//                evalScript(script);
//            });
//        }
//
//        function hasChanceOfBeingBoosted() {
//            return document.querySelector("[hx-boost], [data-hx-boost]");
//        }
//
//        function findElementsToProcess(elt) {
//            if (elt.querySelectorAll) {
//                var boostedElts = hasChanceOfBeingBoosted() ? ", a, form" : "";
//                var results = elt.querySelectorAll(VERB_SELECTOR + boostedElts + ", [hx-sse], [data-hx-sse], [hx-ws]," +
//                    " [data-hx-ws], [hx-ext], [data-hx-ext], [hx-trigger], [data-hx-trigger], [hx-on], [data-hx-on]");
//                return results;
//            } else {
//                return [];
//            }
//        }
//
//        function initButtonTracking(form){
//            var maybeSetLastButtonClicked = function(evt){
//                var elt = closest(evt.target, "button, input[type='submit']");
//                if (elt !== null) {
//                    var internalData = getInternalData(form);
//                    internalData.lastButtonClicked = elt;
//                }
//            };
//
//            // need to handle both click and focus in:
//            //   focusin - in case someone tabs in to a button and hits the space bar
//            //   click - on OSX buttons do not focus on click see https://bugs.webkit.org/show_bug.cgi?id=13724
//
//            form.addEventListener('click', maybeSetLastButtonClicked)
//            form.addEventListener('focusin', maybeSetLastButtonClicked)
//            form.addEventListener('focusout', function(evt){
//                var internalData = getInternalData(form);
//                internalData.lastButtonClicked = null;
//            })
//        }
//
//        function countCurlies(line) {
//            var tokens = tokenizeString(line);
//            var netCurlies = 0;
//            for (let i = 0; i < tokens.length; i++) {
//                const token = tokens[i];
//                if (token === "{") {
//                    netCurlies++;
//                } else if (token === "}") {
//                    netCurlies--;
//                }
//            }
//            return netCurlies;
//        }
//
//        function addHxOnEventHandler(elt, eventName, code) {
//            var nodeData = getInternalData(elt);
//            nodeData.onHandlers = [];
//            var func = new Function("event", code + "; return;");
//            var listener = elt.addEventListener(eventName, function (e) {
//                return func.call(elt, e);
//            });
//            nodeData.onHandlers.push({event:eventName, listener:listener});
//            return {nodeData, code, func, listener};
//        }
//
//        function processHxOn(elt) {
//            var hxOnValue = getAttributeValue(elt, 'hx-on');
//            if (hxOnValue) {
//                var handlers = {}
//                var lines = hxOnValue.split("\n");
//                var currentEvent = null;
//                var curlyCount = 0;
//                while (lines.length > 0) {
//                    var line = lines.shift();
//                    var match = line.match(/^\s*([a-zA-Z:\-]+:)(.*)/);
//                    if (curlyCount === 0 && match) {
//                        line.split(":")
//                        currentEvent = match[1].slice(0, -1); // strip last colon
//                        handlers[currentEvent] = match[2];
//                    } else {
//                        handlers[currentEvent] += line;
//                    }
//                    curlyCount += countCurlies(line);
//                }
//
//                for (var eventName in handlers) {
//                    addHxOnEventHandler(elt, eventName, handlers[eventName]);
//                }
//            }
//        }
//
//        function initNode(elt) {
//            if (elt.closest && elt.closest(htmx.config.disableSelector)) {
//                return;
//            }
//            var nodeData = getInternalData(elt);
//            if (nodeData.initHash !== attributeHash(elt)) {
//
//                nodeData.initHash = attributeHash(elt);
//
//                // clean up any previously processed info
//                deInitNode(elt);
//
//                processHxOn(elt);
//
//                triggerEvent(elt, "htmx:beforeProcessNode")
//
//                if (elt.value) {
//                    nodeData.lastValue = elt.value;
//                }
//
//                var triggerSpecs = getTriggerSpecs(elt);
//                var hasExplicitHttpAction = processVerbs(elt, nodeData, triggerSpecs);
//
//                if (!hasExplicitHttpAction) {
//                    if (getClosestAttributeValue(elt, "hx-boost") === "true") {
//                        boostElement(elt, nodeData, triggerSpecs);
//                    } else if (hasAttribute(elt, 'hx-trigger')) {
//                        triggerSpecs.forEach(function (triggerSpec) {
//                            // For "naked" triggers, don't do anything at all
//                            addTriggerHandler(elt, triggerSpec, nodeData, function () {
//                            })
//                        })
//                    }
//                }
//
//                if (elt.tagName === "FORM") {
//                    initButtonTracking(elt);
//                }
//
//                var sseInfo = getAttributeValue(elt, 'hx-sse');
//                if (sseInfo) {
//                    processSSEInfo(elt, nodeData, sseInfo);
//                }
//
//                var wsInfo = getAttributeValue(elt, 'hx-ws');
//                if (wsInfo) {
//                    processWebSocketInfo(elt, nodeData, wsInfo);
//                }
//                triggerEvent(elt, "htmx:afterProcessNode");
//            }
//        }
//
//        function processNode(elt) {
//            elt = resolveTarget(elt);
//            initNode(elt);
//            forEach(findElementsToProcess(elt), function(child) { initNode(child) });
//        }
//
//        //====================================================================
//        // Event/Log Support
//        //====================================================================
//
//        function kebabEventName(str) {
//            return str.replace(/([a-z0-9])([A-Z])/g, '$1-$2').toLowerCase();
//        }
//
//        function makeEvent(eventName, detail) {
//            var evt;
//            if (window.CustomEvent && typeof window.CustomEvent === 'function') {
//                evt = new CustomEvent(eventName, {bubbles: true, cancelable: true, detail: detail});
//            } else {
//                evt = getDocument().createEvent('CustomEvent');
//                evt.initCustomEvent(eventName, true, true, detail);
//            }
//            return evt;
//        }
//
//        function triggerErrorEvent(elt, eventName, detail) {
//            triggerEvent(elt, eventName, mergeObjects({error:eventName}, detail));
//        }
//
//        function ignoreEventForLogging(eventName) {
//            return eventName === "htmx:afterProcessNode"
//        }
//
//        /**
//         * `withExtensions` locates all active extensions for a provided element, then
//         * executes the provided function using each of the active extensions.  It should
//         * be called internally at every extendable execution point in htmx.
//         *
//         * @param {HTMLElement} elt
//         * @param {(extension:import("./htmx").HtmxExtension) => void} toDo
//         * @returns void
//         */
//        function withExtensions(elt, toDo) {
//            forEach(getExtensions(elt), function(extension){
//                try {
//                    toDo(extension);
//                } catch (e) {
//                    logError(e);
//                }
//            });
//        }
//
//        function logError(msg) {
//            if(console.error) {
//                console.error(msg);
//            } else if (console.log) {
//                console.log("ERROR: ", msg);
//            }
//        }
//
//        function triggerEvent(elt, eventName, detail) {
//            elt = resolveTarget(elt);
//            if (detail == null) {
//                detail = {};
//            }
//            detail["elt"] = elt;
//            var event = makeEvent(eventName, detail);
//            if (htmx.logger && !ignoreEventForLogging(eventName)) {
//                htmx.logger(elt, eventName, detail);
//            }
//            if (detail.error) {
//                logError(detail.error);
//                triggerEvent(elt, "htmx:error", {errorInfo:detail})
//            }
//            var eventResult = elt.dispatchEvent(event);
//            var kebabName = kebabEventName(eventName);
//            if (eventResult && kebabName !== eventName) {
//                var kebabedEvent = makeEvent(kebabName, event.detail);
//                eventResult = eventResult && elt.dispatchEvent(kebabedEvent)
//            }
//            withExtensions(elt, function (extension) {
//                eventResult = eventResult && (extension.onEvent(eventName, event) !== false)
//            });
//            return eventResult;
//        }
//
//        //====================================================================
//        // History Support
//        //====================================================================
//        var currentPathForHistory = location.pathname+location.search;
//
//        function getHistoryElement() {
//            var historyElt = getDocument().querySelector('[hx-history-elt],[data-hx-history-elt]');
//            return historyElt || getDocument().body;
//        }
//
//        function saveToHistoryCache(url, content, title, scroll) {
//            if (!canAccessLocalStorage()) {
//                return;
//            }
//
//            url = normalizePath(url);
//
//            var historyCache = parseJSON(localStorage.getItem("htmx-history-cache")) || [];
//            for (var i = 0; i < historyCache.length; i++) {
//                if (historyCache[i].url === url) {
//                    historyCache.splice(i, 1);
//                    break;
//                }
//            }
//            var newHistoryItem = {url:url, content: content, title:title, scroll:scroll};
//            triggerEvent(getDocument().body, "htmx:historyItemCreated", {item:newHistoryItem, cache: historyCache})
//            historyCache.push(newHistoryItem)
//            while (historyCache.length > htmx.config.historyCacheSize) {
//                historyCache.shift();
//            }
//            while(historyCache.length > 0){
//                try {
//                    localStorage.setItem("htmx-history-cache", JSON.stringify(historyCache));
//                    break;
//                } catch (e) {
//                    triggerErrorEvent(getDocument().body, "htmx:historyCacheError", {cause:e, cache: historyCache})
//                    historyCache.shift(); // shrink the cache and retry
//                }
//            }
//        }
//
//        function getCachedHistory(url) {
//            if (!canAccessLocalStorage()) {
//                return null;
//            }
//
//            url = normalizePath(url);
//
//            var historyCache = parseJSON(localStorage.getItem("htmx-history-cache")) || [];
//            for (var i = 0; i < historyCache.length; i++) {
//                if (historyCache[i].url === url) {
//                    return historyCache[i];
//                }
//            }
//            return null;
//        }
//
//        function cleanInnerHtmlForHistory(elt) {
//            var className = htmx.config.requestClass;
//            var clone = elt.cloneNode(true);
//            forEach(findAll(clone, "." + className), function(child){
//                removeClassFromElement(child, className);
//            });
//            return clone.innerHTML;
//        }
//
//        function saveCurrentPageToHistory() {
//            var elt = getHistoryElement();
//            var path = currentPathForHistory || location.pathname+location.search;
//
//            // Allow history snapshot feature to be disabled where hx-history="false"
//            // is present *anywhere* in the current document we're about to save,
//            // so we can prevent privileged data entering the cache.
//            // The page will still be reachable as a history entry, but htmx will fetch it
//            // live from the server onpopstate rather than look in the localStorage cache
//            var disableHistoryCache = getDocument().querySelector('[hx-history="false" i],[data-hx-history="false" i]');
//            if (!disableHistoryCache) {
//                triggerEvent(getDocument().body, "htmx:beforeHistorySave", {path: path, historyElt: elt});
//                saveToHistoryCache(path, cleanInnerHtmlForHistory(elt), getDocument().title, window.scrollY);
//            }
//
//            if (htmx.config.historyEnabled) history.replaceState({htmx: true}, getDocument().title, window.location.href);
//        }
//
//        function pushUrlIntoHistory(path) {
//            // remove the cache buster parameter, if any
//            if (htmx.config.getCacheBusterParam) {
//                path = path.replace(/org\.htmx\.cache-buster=[^&]*&?/, '')
//                if (path.endsWith('&') || path.endsWith("?")) {
//                    path = path.slice(0, -1);
//                }
//            }
//            if(htmx.config.historyEnabled) {
//                history.pushState({htmx:true}, "", path);
//            }
//            currentPathForHistory = path;
//        }
//
//        function replaceUrlInHistory(path) {
//            if(htmx.config.historyEnabled)  history.replaceState({htmx:true}, "", path);
//            currentPathForHistory = path;
//        }
//
//        function settleImmediately(tasks) {
//            forEach(tasks, function (task) {
//                task.call();
//            });
//        }
//
//        function loadHistoryFromServer(path) {
//            var request = new XMLHttpRequest();
//            var details = {path: path, xhr:request};
//            triggerEvent(getDocument().body, "htmx:historyCacheMiss", details);
//            request.open('GET', path, true);
//            request.setRequestHeader("HX-History-Restore-Request", "true");
//            request.onload = function () {
//                if (this.status >= 200 && this.status < 400) {
//                    triggerEvent(getDocument().body, "htmx:historyCacheMissLoad", details);
//                    var fragment = makeFragment(this.response);
//                    // @ts-ignore
//                    fragment = fragment.querySelector('[hx-history-elt],[data-hx-history-elt]') || fragment;
//                    var historyElement = getHistoryElement();
//                    var settleInfo = makeSettleInfo(historyElement);
//                    var title = findTitle(this.response);
//                    if (title) {
//                        var titleElt = find("title");
//                        if (titleElt) {
//                            titleElt.innerHTML = title;
//                        } else {
//                            window.document.title = title;
//                        }
//                    }
//                    // @ts-ignore
//                    swapInnerHTML(historyElement, fragment, settleInfo)
//                    settleImmediately(settleInfo.tasks);
//                    currentPathForHistory = path;
//                    triggerEvent(getDocument().body, "htmx:historyRestore", {path: path, cacheMiss:true, serverResponse:this.response});
//                } else {
//                    triggerErrorEvent(getDocument().body, "htmx:historyCacheMissLoadError", details);
//                }
//            };
//            request.send();
//        }
//
//        function restoreHistory(path) {
//            saveCurrentPageToHistory();
//            path = path || location.pathname+location.search;
//            var cached = getCachedHistory(path);
//            if (cached) {
//                var fragment = makeFragment(cached.content);
//                var historyElement = getHistoryElement();
//                var settleInfo = makeSettleInfo(historyElement);
//                swapInnerHTML(historyElement, fragment, settleInfo)
//                settleImmediately(settleInfo.tasks);
//                document.title = cached.title;
//                window.scrollTo(0, cached.scroll);
//                currentPathForHistory = path;
//                triggerEvent(getDocument().body, "htmx:historyRestore", {path:path, item:cached});
//            } else {
//                if (htmx.config.refreshOnHistoryMiss) {
//
//                    // @ts-ignore: optional parameter in reload() function throws error
//                    window.location.reload(true);
//                } else {
//                    loadHistoryFromServer(path);
//                }
//            }
//        }
//
//        function addRequestIndicatorClasses(elt) {
//            var indicators = findAttributeTargets(elt, 'hx-indicator');
//            if (indicators == null) {
//                indicators = [elt];
//            }
//            forEach(indicators, function (ic) {
//                var internalData = getInternalData(ic);
//                internalData.requestCount = (internalData.requestCount || 0) + 1;
//                ic.classList["add"].call(ic.classList, htmx.config.requestClass);
//            });
//            return indicators;
//        }
//
//        function removeRequestIndicatorClasses(indicators) {
//            forEach(indicators, function (ic) {
//                var internalData = getInternalData(ic);
//                internalData.requestCount = (internalData.requestCount || 0) - 1;
//                if (internalData.requestCount === 0) {
//                    ic.classList["remove"].call(ic.classList, htmx.config.requestClass);
//                }
//            });
//        }
//
//        //====================================================================
//        // Input Value Processing
//        //====================================================================
//
//        function haveSeenNode(processed, elt) {
//            for (var i = 0; i < processed.length; i++) {
//                var node = processed[i];
//                if (node.isSameNode(elt)) {
//                    return true;
//                }
//            }
//            return false;
//        }
//
//        function shouldInclude(elt) {
//            if(elt.name === "" || elt.name == null || elt.disabled) {
//                return false;
//            }
//            // ignore "submitter" types (see jQuery src/serialize.js)
//            if (elt.type === "button" || elt.type === "submit" || elt.tagName === "image" || elt.tagName === "reset" || elt.tagName === "file" ) {
//                return false;
//            }
//            if (elt.type === "checkbox" || elt.type === "radio" ) {
//                return elt.checked;
//            }
//            return true;
//        }
//
//        function processInputValue(processed, values, errors, elt, validate) {
//            if (elt == null || haveSeenNode(processed, elt)) {
//                return;
//            } else {
//                processed.push(elt);
//            }
//            if (shouldInclude(elt)) {
//                var name = getRawAttribute(elt,"name");
//                var value = elt.value;
//                if (elt.multiple) {
//                    value = toArray(elt.querySelectorAll("option:checked")).map(function (e) { return e.value });
//                }
//                // include file inputs
//                if (elt.files) {
//                    value = toArray(elt.files);
//                }
//                // This is a little ugly because both the current value of the named value in the form
//                // and the new value could be arrays, so we have to handle all four cases :/
//                if (name != null && value != null) {
//                    var current = values[name];
//                    if (current !== undefined) {
//                        if (Array.isArray(current)) {
//                            if (Array.isArray(value)) {
//                                values[name] = current.concat(value);
//                            } else {
//                                current.push(value);
//                            }
//                        } else {
//                            if (Array.isArray(value)) {
//                                values[name] = [current].concat(value);
//                            } else {
//                                values[name] = [current, value];
//                            }
//                        }
//                    } else {
//                        values[name] = value;
//                    }
//                }
//                if (validate) {
//                    validateElement(elt, errors);
//                }
//            }
//            if (matches(elt, 'form')) {
//                var inputs = elt.elements;
//                forEach(inputs, function(input) {
//                    processInputValue(processed, values, errors, input, validate);
//                });
//            }
//        }
//
//        function validateElement(element, errors) {
//            if (element.willValidate) {
//                triggerEvent(element, "htmx:validation:validate")
//                if (!element.checkValidity()) {
//                    errors.push({elt: element, message:element.validationMessage, validity:element.validity});
//                    triggerEvent(element, "htmx:validation:failed", {message:element.validationMessage, validity:element.validity})
//                }
//            }
//        }
//
//        /**
//         * @param {HTMLElement} elt
//         * @param {string} verb
//         */
//        function getInputValues(elt, verb) {
//            var processed = [];
//            var values = {};
//            var formValues = {};
//            var errors = [];
//            var internalData = getInternalData(elt);
//
//            // only validate when form is directly submitted and novalidate or formnovalidate are not set
//            // or if the element has an explicit hx-validate="true" on it
//            var validate = (matches(elt, 'form') && elt.noValidate !== true) || getAttributeValue(elt, "hx-validate") === "true";
//            if (internalData.lastButtonClicked) {
//                validate = validate && internalData.lastButtonClicked.formNoValidate !== true;
//            }
//
//            // for a non-GET include the closest form
//            if (verb !== 'get') {
//                processInputValue(processed, formValues, errors, closest(elt, 'form'), validate);
//            }
//
//            // include the element itself
//            processInputValue(processed, values, errors, elt, validate);
//
//            // if a button or submit was clicked last, include its value
//            if (internalData.lastButtonClicked) {
//                var name = getRawAttribute(internalData.lastButtonClicked,"name");
//                if (name) {
//                    values[name] = internalData.lastButtonClicked.value;
//                }
//            }
//
//            // include any explicit includes
//            var includes = findAttributeTargets(elt, "hx-include");
//            forEach(includes, function(node) {
//                processInputValue(processed, values, errors, node, validate);
//                // if a non-form is included, include any input values within it
//                if (!matches(node, 'form')) {
//                    forEach(node.querySelectorAll(INPUT_SELECTOR), function (descendant) {
//                        processInputValue(processed, values, errors, descendant, validate);
//                    })
//                }
//            });
//
//            // form values take precedence, overriding the regular values
//            values = mergeObjects(values, formValues);
//
//            return {errors:errors, values:values};
//        }
//
//        function appendParam(returnStr, name, realValue) {
//            if (returnStr !== "") {
//                returnStr += "&";
//            }
//            if (String(realValue) === "[object Object]") {
//                realValue = JSON.stringify(realValue);
//            }
//            var s = encodeURIComponent(realValue);
//            returnStr += encodeURIComponent(name) + "=" + s;
//            return returnStr;
//        }
//
//        function urlEncode(values) {
//            var returnStr = "";
//            for (var name in values) {
//                if (values.hasOwnProperty(name)) {
//                    var value = values[name];
//                    if (Array.isArray(value)) {
//                        forEach(value, function(v) {
//                            returnStr = appendParam(returnStr, name, v);
//                        });
//                    } else {
//                        returnStr = appendParam(returnStr, name, value);
//                    }
//                }
//            }
//            return returnStr;
//        }
//
//        function makeFormData(values) {
//            var formData = new FormData();
//            for (var name in values) {
//                if (values.hasOwnProperty(name)) {
//                    var value = values[name];
//                    if (Array.isArray(value)) {
//                        forEach(value, function(v) {
//                            formData.append(name, v);
//                        });
//                    } else {
//                        formData.append(name, value);
//                    }
//                }
//            }
//            return formData;
//        }
//
//        //====================================================================
//        // Ajax
//        //====================================================================
//
//        /**
//         * @param {HTMLElement} elt
//         * @param {HTMLElement} target
//         * @param {string} prompt
//         * @returns {Object} // TODO: Define/Improve HtmxHeaderSpecification
//         */
//        function getHeaders(elt, target, prompt) {
//            var headers = {
//                "HX-Request" : "true",
//                "HX-Trigger" : getRawAttribute(elt, "id"),
//                "HX-Trigger-Name" : getRawAttribute(elt, "name"),
//                "HX-Target" : getAttributeValue(target, "id"),
//                "HX-Current-URL" : getDocument().location.href,
//            }
//            getValuesForElement(elt, "hx-headers", false, headers)
//            if (prompt !== undefined) {
//                headers["HX-Prompt"] = prompt;
//            }
//            if (getInternalData(elt).boosted) {
//                headers["HX-Boosted"] = "true";
//            }
//            return headers;
//        }
//
//        /**
//         * filterValues takes an object containing form input values
//         * and returns a new object that only contains keys that are
//         * specified by the closest "hx-params" attribute
//         * @param {Object} inputValues
//         * @param {HTMLElement} elt
//         * @returns {Object}
//         */
//        function filterValues(inputValues, elt) {
//            var paramsValue = getClosestAttributeValue(elt, "hx-params");
//            if (paramsValue) {
//                if (paramsValue === "none") {
//                    return {};
//                } else if (paramsValue === "*") {
//                    return inputValues;
//                } else if(paramsValue.indexOf("not ") === 0) {
//                    forEach(paramsValue.substr(4).split(","), function (name) {
//                        name = name.trim();
//                        delete inputValues[name];
//                    });
//                    return inputValues;
//                } else {
//                    var newValues = {}
//                    forEach(paramsValue.split(","), function (name) {
//                        name = name.trim();
//                        newValues[name] = inputValues[name];
//                    });
//                    return newValues;
//                }
//            } else {
//                return inputValues;
//            }
//        }
//
//        function isAnchorLink(elt) {
//          return getRawAttribute(elt, 'href') && getRawAttribute(elt, 'href').indexOf("#") >=0
//        }
//
//        /**
//         *
//         * @param {HTMLElement} elt
//         * @param {string} swapInfoOverride
//         * @returns {import("./htmx").HtmxSwapSpecification}
//         */
//        function getSwapSpecification(elt, swapInfoOverride) {
//            var swapInfo = swapInfoOverride ? swapInfoOverride : getClosestAttributeValue(elt, "hx-swap");
//            var swapSpec = {
//                "swapStyle" : getInternalData(elt).boosted ? 'innerHTML' : htmx.config.defaultSwapStyle,
//                "swapDelay" : htmx.config.defaultSwapDelay,
//                "settleDelay" : htmx.config.defaultSettleDelay
//            }
//            if (getInternalData(elt).boosted && !isAnchorLink(elt)) {
//              swapSpec["show"] = "top"
//            }
//            if (swapInfo) {
//                var split = splitOnWhitespace(swapInfo);
//                if (split.length > 0) {
//                    swapSpec["swapStyle"] = split[0];
//                    for (var i = 1; i < split.length; i++) {
//                        var modifier = split[i];
//                        if (modifier.indexOf("swap:") === 0) {
//                            swapSpec["swapDelay"] = parseInterval(modifier.substr(5));
//                        }
//                        if (modifier.indexOf("settle:") === 0) {
//                            swapSpec["settleDelay"] = parseInterval(modifier.substr(7));
//                        }
//                        if (modifier.indexOf("transition:") === 0) {
//                            swapSpec["transition"] = modifier.substr(11) === "true";
//                        }
//                        if (modifier.indexOf("scroll:") === 0) {
//                            var scrollSpec = modifier.substr(7);
//                            var splitSpec = scrollSpec.split(":");
//                            var scrollVal = splitSpec.pop();
//                            var selectorVal = splitSpec.length > 0 ? splitSpec.join(":") : null;
//                            swapSpec["scroll"] = scrollVal;
//                            swapSpec["scrollTarget"] = selectorVal;
//                        }
//                        if (modifier.indexOf("show:") === 0) {
//                            var showSpec = modifier.substr(5);
//                            var splitSpec = showSpec.split(":");
//                            var showVal = splitSpec.pop();
//                            var selectorVal = splitSpec.length > 0 ? splitSpec.join(":") : null;
//                            swapSpec["show"] = showVal;
//                            swapSpec["showTarget"] = selectorVal;
//                        }
//                        if (modifier.indexOf("focus-scroll:") === 0) {
//                            var focusScrollVal = modifier.substr("focus-scroll:".length);
//                            swapSpec["focusScroll"] = focusScrollVal == "true";
//                        }
//                    }
//                }
//            }
//            return swapSpec;
//        }
//
//        function usesFormData(elt) {
//            return getClosestAttributeValue(elt, "hx-encoding") === "multipart/form-data" ||
//                (matches(elt, "form") && getRawAttribute(elt, 'enctype') === "multipart/form-data");
//        }
//
//        function encodeParamsForBody(xhr, elt, filteredParameters) {
//            var encodedParameters = null;
//            withExtensions(elt, function (extension) {
//                if (encodedParameters == null) {
//                    encodedParameters = extension.encodeParameters(xhr, filteredParameters, elt);
//                }
//            });
//            if (encodedParameters != null) {
//                return encodedParameters;
//            } else {
//                if (usesFormData(elt)) {
//                    return makeFormData(filteredParameters);
//                } else {
//                    return urlEncode(filteredParameters);
//                }
//            }
//        }
//
//        /**
//         *
//         * @param {Element} target
//         * @returns {import("./htmx").HtmxSettleInfo}
//         */
//        function makeSettleInfo(target) {
//            return {tasks: [], elts: [target]};
//        }
//
//        function updateScrollState(content, swapSpec) {
//            var first = content[0];
//            var last = content[content.length - 1];
//            if (swapSpec.scroll) {
//                var target = null;
//                if (swapSpec.scrollTarget) {
//                    target = querySelectorExt(first, swapSpec.scrollTarget);
//                }
//                if (swapSpec.scroll === "top" && (first || target)) {
//                    target = target || first;
//                    target.scrollTop = 0;
//                }
//                if (swapSpec.scroll === "bottom" && (last || target)) {
//                    target = target || last;
//                    target.scrollTop = target.scrollHeight;
//                }
//            }
//            if (swapSpec.show) {
//                var target = null;
//                if (swapSpec.showTarget) {
//                    var targetStr = swapSpec.showTarget;
//                    if (swapSpec.showTarget === "window") {
//                        targetStr = "body";
//                    }
//                    target = querySelectorExt(first, targetStr);
//                }
//                if (swapSpec.show === "top" && (first || target)) {
//                    target = target || first;
//                    target.scrollIntoView({block:'start', behavior: htmx.config.scrollBehavior});
//                }
//                if (swapSpec.show === "bottom" && (last || target)) {
//                    target = target || last;
//                    target.scrollIntoView({block:'end', behavior: htmx.config.scrollBehavior});
//                }
//            }
//        }
//
//        /**
//         * @param {HTMLElement} elt
//         * @param {string} attr
//         * @param {boolean=} evalAsDefault
//         * @param {Object=} values
//         * @returns {Object}
//         */
//        function getValuesForElement(elt, attr, evalAsDefault, values) {
//            if (values == null) {
//                values = {};
//            }
//            if (elt == null) {
//                return values;
//            }
//            var attributeValue = getAttributeValue(elt, attr);
//            if (attributeValue) {
//                var str = attributeValue.trim();
//                var evaluateValue = evalAsDefault;
//                if (str === "unset") {
//                    return null;
//                }
//                if (str.indexOf("javascript:") === 0) {
//                    str = str.substr(11);
//                    evaluateValue = true;
//                } else if (str.indexOf("js:") === 0) {
//                    str = str.substr(3);
//                    evaluateValue = true;
//                }
//                if (str.indexOf('{') !== 0) {
//                    str = "{" + str + "}";
//                }
//                var varsValues;
//                if (evaluateValue) {
//                    varsValues = maybeEval(elt,function () {return Function("return (" + str + ")")();}, {});
//                } else {
//                    varsValues = parseJSON(str);
//                }
//                for (var key in varsValues) {
//                    if (varsValues.hasOwnProperty(key)) {
//                        if (values[key] == null) {
//                            values[key] = varsValues[key];
//                        }
//                    }
//                }
//            }
//            return getValuesForElement(parentElt(elt), attr, evalAsDefault, values);
//        }
//
//        function maybeEval(elt, toEval, defaultVal) {
//            if (htmx.config.allowEval) {
//                return toEval();
//            } else {
//                triggerErrorEvent(elt, 'htmx:evalDisallowedError');
//                return defaultVal;
//            }
//        }
//
//        /**
//         * @param {HTMLElement} elt
//         * @param {*} expressionVars
//         * @returns
//         */
//        function getHXVarsForElement(elt, expressionVars) {
//            return getValuesForElement(elt, "hx-vars", true, expressionVars);
//        }
//
//        /**
//         * @param {HTMLElement} elt
//         * @param {*} expressionVars
//         * @returns
//         */
//        function getHXValsForElement(elt, expressionVars) {
//            return getValuesForElement(elt, "hx-vals", false, expressionVars);
//        }
//
//        /**
//         * @param {HTMLElement} elt
//         * @returns {Object}
//         */
//        function getExpressionVars(elt) {
//            return mergeObjects(getHXVarsForElement(elt), getHXValsForElement(elt));
//        }
//
//        function safelySetHeaderValue(xhr, header, headerValue) {
//            if (headerValue !== null) {
//                try {
//                    xhr.setRequestHeader(header, headerValue);
//                } catch (e) {
//                    // On an exception, try to set the header URI encoded instead
//                    xhr.setRequestHeader(header, encodeURIComponent(headerValue));
//                    xhr.setRequestHeader(header + "-URI-AutoEncoded", "true");
//                }
//            }
//        }
//
//        function getPathFromResponse(xhr) {
//            // NB: IE11 does not support this stuff
//            if (xhr.responseURL && typeof(URL) !== "undefined") {
//                try {
//                    var url = new URL(xhr.responseURL);
//                    return url.pathname + url.search;
//                } catch (e) {
//                    triggerErrorEvent(getDocument().body, "htmx:badResponseUrl", {url: xhr.responseURL});
//                }
//            }
//        }
//
//        function hasHeader(xhr, regexp) {
//            return xhr.getAllResponseHeaders().match(regexp);
//        }
//
//        function ajaxHelper(verb, path, context) {
//            verb = verb.toLowerCase();
//            if (context) {
//                if (context instanceof Element || isType(context, 'String')) {
//                    return issueAjaxRequest(verb, path, null, null, {
//                        targetOverride: resolveTarget(context),
//                        returnPromise: true
//                    });
//                } else {
//                    return issueAjaxRequest(verb, path, resolveTarget(context.source), context.event,
//                        {
//                            handler : context.handler,
//                            headers : context.headers,
//                            values : context.values,
//                            targetOverride: resolveTarget(context.target),
//                            swapOverride: context.swap,
//                            returnPromise: true
//                        });
//                }
//            } else {
//                return issueAjaxRequest(verb, path, null, null, {
//                        returnPromise: true
//                });
//            }
//        }
//
//        function hierarchyForElt(elt) {
//            var arr = [];
//            while (elt) {
//                arr.push(elt);
//                elt = elt.parentElement;
//            }
//            return arr;
//        }
//
//        function issueAjaxRequest(verb, path, elt, event, etc, confirmed) {
//            var resolve = null;
//            var reject = null;
//            etc = etc != null ? etc : {};
//            if(etc.returnPromise && typeof Promise !== "undefined"){
//                var promise = new Promise(function (_resolve, _reject) {
//                    resolve = _resolve;
//                    reject = _reject;
//                });
//            }
//            if(elt == null) {
//                elt = getDocument().body;
//            }
//            var responseHandler = etc.handler || handleAjaxResponse;
//
//            if (!bodyContains(elt)) {
//                return; // do not issue requests for elements removed from the DOM
//            }
//            var target = etc.targetOverride || getTarget(elt);
//            if (target == null || target == DUMMY_ELT) {
//                triggerErrorEvent(elt, 'htmx:targetError', {target: getAttributeValue(elt, "hx-target")});
//                return;
//            }
//
//            // allow event-based confirmation w/ a callback
//            if (!confirmed) {
//                var issueRequest = function() {
//                    return issueAjaxRequest(verb, path, elt, event, etc, true);
//                }
//                var confirmDetails = {target: target, elt: elt, path: path, verb: verb, triggeringEvent: event, etc: etc, issueRequest: issueRequest};
//                if (triggerEvent(elt, 'htmx:confirm', confirmDetails) === false) {
//                    return;
//                }
//            }
//
//            var syncElt = elt;
//            var eltData = getInternalData(elt);
//            var syncStrategy = getClosestAttributeValue(elt, "hx-sync");
//            var queueStrategy = null;
//            var abortable = false;
//            if (syncStrategy) {
//                var syncStrings = syncStrategy.split(":");
//                var selector = syncStrings[0].trim();
//                if (selector === "this") {
//                    syncElt = findThisElement(elt, 'hx-sync');
//                } else {
//                    syncElt = querySelectorExt(elt, selector);
//                }
//                // default to the drop strategy
//                syncStrategy = (syncStrings[1] || 'drop').trim();
//                eltData = getInternalData(syncElt);
//                if (syncStrategy === "drop" && eltData.xhr && eltData.abortable !== true) {
//                    return;
//                } else if (syncStrategy === "abort") {
//                    if (eltData.xhr) {
//                        return;
//                    } else {
//                        abortable = true;
//                    }
//                } else if (syncStrategy === "replace") {
//                    triggerEvent(syncElt, 'htmx:abort'); // abort the current request and continue
//                } else if (syncStrategy.indexOf("queue") === 0) {
//                    var queueStrArray = syncStrategy.split(" ");
//                    queueStrategy = (queueStrArray[1] || "last").trim();
//                }
//            }
//
//            if (eltData.xhr) {
//                if (eltData.abortable) {
//                    triggerEvent(syncElt, 'htmx:abort'); // abort the current request and continue
//                } else {
//                    if(queueStrategy == null){
//                        if (event) {
//                            var eventData = getInternalData(event);
//                            if (eventData && eventData.triggerSpec && eventData.triggerSpec.queue) {
//                                queueStrategy = eventData.triggerSpec.queue;
//                            }
//                        }
//                        if (queueStrategy == null) {
//                            queueStrategy = "last";
//                        }
//                    }
//                    if (eltData.queuedRequests == null) {
//                        eltData.queuedRequests = [];
//                    }
//                    if (queueStrategy === "first" && eltData.queuedRequests.length === 0) {
//                        eltData.queuedRequests.push(function () {
//                            issueAjaxRequest(verb, path, elt, event, etc)
//                        });
//                    } else if (queueStrategy === "all") {
//                        eltData.queuedRequests.push(function () {
//                            issueAjaxRequest(verb, path, elt, event, etc)
//                        });
//                    } else if (queueStrategy === "last") {
//                        eltData.queuedRequests = []; // dump existing queue
//                        eltData.queuedRequests.push(function () {
//                            issueAjaxRequest(verb, path, elt, event, etc)
//                        });
//                    }
//                    return;
//                }
//            }
//
//            var xhr = new XMLHttpRequest();
//            eltData.xhr = xhr;
//            eltData.abortable = abortable;
//            var endRequestLock = function(){
//                eltData.xhr = null;
//                eltData.abortable = false;
//                if (eltData.queuedRequests != null &&
//                    eltData.queuedRequests.length > 0) {
//                    var queuedRequest = eltData.queuedRequests.shift();
//                    queuedRequest();
//                }
//            }
//            var promptQuestion = getClosestAttributeValue(elt, "hx-prompt");
//            if (promptQuestion) {
//                var promptResponse = prompt(promptQuestion);
//                // prompt returns null if cancelled and empty string if accepted with no entry
//                if (promptResponse === null ||
//                    !triggerEvent(elt, 'htmx:prompt', {prompt: promptResponse, target:target})) {
//                    maybeCall(resolve);
//                    endRequestLock();
//                    return promise;
//                }
//            }
//
//            var confirmQuestion = getClosestAttributeValue(elt, "hx-confirm");
//            if (confirmQuestion) {
//                if(!confirm(confirmQuestion)) {
//                    maybeCall(resolve);
//                    endRequestLock()
//                    return promise;
//                }
//            }
//
//
//            var headers = getHeaders(elt, target, promptResponse);
//            if (etc.headers) {
//                headers = mergeObjects(headers, etc.headers);
//            }
//            var results = getInputValues(elt, verb);
//            var errors = results.errors;
//            var rawParameters = results.values;
//            if (etc.values) {
//                rawParameters = mergeObjects(rawParameters, etc.values);
//            }
//            var expressionVars = getExpressionVars(elt);
//            var allParameters = mergeObjects(rawParameters, expressionVars);
//            var filteredParameters = filterValues(allParameters, elt);
//
//            if (verb !== 'get' && !usesFormData(elt)) {
//                headers['Content-Type'] = 'application/x-www-form-urlencoded';
//            }
//
//            if (htmx.config.getCacheBusterParam && verb === 'get') {
//                filteredParameters['org.htmx.cache-buster'] = getRawAttribute(target, "id") || "true";
//            }
//
//            // behavior of anchors w/ empty href is to use the current URL
//            if (path == null || path === "") {
//                path = getDocument().location.href;
//            }
//
//
//            var requestAttrValues = getValuesForElement(elt, 'hx-request');
//
//            var eltIsBoosted = getInternalData(elt).boosted;
//            var requestConfig = {
//                boosted: eltIsBoosted,
//                parameters: filteredParameters,
//                unfilteredParameters: allParameters,
//                headers:headers,
//                target:target,
//                verb:verb,
//                errors:errors,
//                withCredentials: etc.credentials || requestAttrValues.credentials || htmx.config.withCredentials,
//                timeout:  etc.timeout || requestAttrValues.timeout || htmx.config.timeout,
//                path:path,
//                triggeringEvent:event
//            };
//
//            if(!triggerEvent(elt, 'htmx:configRequest', requestConfig)){
//                maybeCall(resolve);
//                endRequestLock();
//                return promise;
//            }
//
//            // copy out in case the object was overwritten
//            path = requestConfig.path;
//            verb = requestConfig.verb;
//            headers = requestConfig.headers;
//            filteredParameters = requestConfig.parameters;
//            errors = requestConfig.errors;
//
//            if(errors && errors.length > 0){
//                triggerEvent(elt, 'htmx:validation:halted', requestConfig)
//                maybeCall(resolve);
//                endRequestLock();
//                return promise;
//            }
//
//            var splitPath = path.split("#");
//            var pathNoAnchor = splitPath[0];
//            var anchor = splitPath[1];
//            var finalPathForGet = null;
//            if (verb === 'get') {
//                finalPathForGet = pathNoAnchor;
//                var values = Object.keys(filteredParameters).length !== 0;
//                if (values) {
//                    if (finalPathForGet.indexOf("?") < 0) {
//                        finalPathForGet += "?";
//                    } else {
//                        finalPathForGet += "&";
//                    }
//                    finalPathForGet += urlEncode(filteredParameters);
//                    if (anchor) {
//                        finalPathForGet += "#" + anchor;
//                    }
//                }
//                xhr.open('GET', finalPathForGet, true);
//            } else {
//                xhr.open(verb.toUpperCase(), path, true);
//            }
//
//            xhr.overrideMimeType("text/html");
//            xhr.withCredentials = requestConfig.withCredentials;
//            xhr.timeout = requestConfig.timeout;
//
//            // request headers
//            if (requestAttrValues.noHeaders) {
//                // ignore all headers
//            } else {
//                for (var header in headers) {
//                    if (headers.hasOwnProperty(header)) {
//                        var headerValue = headers[header];
//                        safelySetHeaderValue(xhr, header, headerValue);
//                    }
//                }
//            }
//
//            var responseInfo = {
//                xhr: xhr, target: target, requestConfig: requestConfig, etc: etc, boosted: eltIsBoosted,
//                pathInfo: {
//                    requestPath: path,
//                    finalRequestPath: finalPathForGet || path,
//                    anchor: anchor
//                }
//            };
//
//            xhr.onload = function () {
//                try {
//                    var hierarchy = hierarchyForElt(elt);
//                    responseInfo.pathInfo.responsePath = getPathFromResponse(xhr);
//                    responseHandler(elt, responseInfo);
//                    removeRequestIndicatorClasses(indicators);
//                    triggerEvent(elt, 'htmx:afterRequest', responseInfo);
//                    triggerEvent(elt, 'htmx:afterOnLoad', responseInfo);
//                    // if the body no longer contains the element, trigger the event on the closest parent
//                    // remaining in the DOM
//                    if (!bodyContains(elt)) {
//                        var secondaryTriggerElt = null;
//                        while (hierarchy.length > 0 && secondaryTriggerElt == null) {
//                            var parentEltInHierarchy = hierarchy.shift();
//                            if (bodyContains(parentEltInHierarchy)) {
//                                secondaryTriggerElt = parentEltInHierarchy;
//                            }
//                        }
//                        if (secondaryTriggerElt) {
//                            triggerEvent(secondaryTriggerElt, 'htmx:afterRequest', responseInfo);
//                            triggerEvent(secondaryTriggerElt, 'htmx:afterOnLoad', responseInfo);
//                        }
//                    }
//                    maybeCall(resolve);
//                    endRequestLock();
//                } catch (e) {
//                    triggerErrorEvent(elt, 'htmx:onLoadError', mergeObjects({error:e}, responseInfo));
//                    throw e;
//                }
//            }
//            xhr.onerror = function () {
//                removeRequestIndicatorClasses(indicators);
//                triggerErrorEvent(elt, 'htmx:afterRequest', responseInfo);
//                triggerErrorEvent(elt, 'htmx:sendError', responseInfo);
//                maybeCall(reject);
//                endRequestLock();
//            }
//            xhr.onabort = function() {
//                removeRequestIndicatorClasses(indicators);
//                triggerErrorEvent(elt, 'htmx:afterRequest', responseInfo);
//                triggerErrorEvent(elt, 'htmx:sendAbort', responseInfo);
//                maybeCall(reject);
//                endRequestLock();
//            }
//            xhr.ontimeout = function() {
//                removeRequestIndicatorClasses(indicators);
//                triggerErrorEvent(elt, 'htmx:afterRequest', responseInfo);
//                triggerErrorEvent(elt, 'htmx:timeout', responseInfo);
//                maybeCall(reject);
//                endRequestLock();
//            }
//            if(!triggerEvent(elt, 'htmx:beforeRequest', responseInfo)){
//                maybeCall(resolve);
//                endRequestLock()
//                return promise
//            }
//            var indicators = addRequestIndicatorClasses(elt);
//
//            forEach(['loadstart', 'loadend', 'progress', 'abort'], function(eventName) {
//                forEach([xhr, xhr.upload], function (target) {
//                    target.addEventListener(eventName, function(event){
//                        triggerEvent(elt, "htmx:xhr:" + eventName, {
//                            lengthComputable:event.lengthComputable,
//                            loaded:event.loaded,
//                            total:event.total
//                        });
//                    })
//                });
//            });
//            triggerEvent(elt, 'htmx:beforeSend', responseInfo);
//            xhr.send(verb === 'get' ? null : encodeParamsForBody(xhr, elt, filteredParameters));
//            return promise;
//        }
//
//        function determineHistoryUpdates(elt, responseInfo) {
//
//            var xhr = responseInfo.xhr;
//
//            //===========================================
//            // First consult response headers
//            //===========================================
//            var pathFromHeaders = null;
//            var typeFromHeaders = null;
//            if (hasHeader(xhr,/HX-Push:/i)) {
//                pathFromHeaders = xhr.getResponseHeader("HX-Push");
//                typeFromHeaders = "push";
//            } else if (hasHeader(xhr,/HX-Push-Url:/i)) {
//                pathFromHeaders = xhr.getResponseHeader("HX-Push-Url");
//                typeFromHeaders = "push";
//            } else if (hasHeader(xhr,/HX-Replace-Url:/i)) {
//                pathFromHeaders = xhr.getResponseHeader("HX-Replace-Url");
//                typeFromHeaders = "replace";
//            }
//
//            // if there was a response header, that has priority
//            if (pathFromHeaders) {
//                if (pathFromHeaders === "false") {
//                    return {}
//                } else {
//                    return {
//                        type: typeFromHeaders,
//                        path : pathFromHeaders
//                    }
//                }
//            }
//
//            //===========================================
//            // Next resolve via DOM values
//            //===========================================
//            var requestPath =  responseInfo.pathInfo.finalRequestPath;
//            var responsePath =  responseInfo.pathInfo.responsePath;
//
//            var pushUrl = getClosestAttributeValue(elt, "hx-push-url");
//            var replaceUrl = getClosestAttributeValue(elt, "hx-replace-url");
//            var elementIsBoosted = getInternalData(elt).boosted;
//
//            var saveType = null;
//            var path = null;
//
//            if (pushUrl) {
//                saveType = "push";
//                path = pushUrl;
//            } else if (replaceUrl) {
//                saveType = "replace";
//                path = replaceUrl;
//            } else if (elementIsBoosted) {
//                saveType = "push";
//                path = responsePath || requestPath; // if there is no response path, go with the original request path
//            }
//
//            if (path) {
//                // false indicates no push, return empty object
//                if (path === "false") {
//                    return {};
//                }
//
//                // true indicates we want to follow wherever the server ended up sending us
//                if (path === "true") {
//                    path = responsePath || requestPath; // if there is no response path, go with the original request path
//                }
//
//                // restore any anchor associated with the request
//                if (responseInfo.pathInfo.anchor &&
//                    path.indexOf("#") === -1) {
//                    path = path + "#" + responseInfo.pathInfo.anchor;
//                }
//
//                return {
//                    type:saveType,
//                    path: path
//                }
//            } else {
//                return {};
//            }
//        }
//
//        function handleAjaxResponse(elt, responseInfo) {
//            var xhr = responseInfo.xhr;
//            var target = responseInfo.target;
//            var etc = responseInfo.etc;
//
//            if (!triggerEvent(elt, 'htmx:beforeOnLoad', responseInfo)) return;
//
//            if (hasHeader(xhr, /HX-Trigger:/i)) {
//                handleTrigger(xhr, "HX-Trigger", elt);
//            }
//
//            if (hasHeader(xhr, /HX-Location:/i)) {
//                saveCurrentPageToHistory();
//                var redirectPath = xhr.getResponseHeader("HX-Location");
//                var swapSpec;
//                if (redirectPath.indexOf("{") === 0) {
//                    swapSpec = parseJSON(redirectPath);
//                    // what's the best way to throw an error if the user didn't include this
//                    redirectPath = swapSpec['path'];
//                    delete swapSpec['path'];
//                }
//                ajaxHelper('GET', redirectPath, swapSpec).then(function(){
//                    pushUrlIntoHistory(redirectPath);
//                });
//                return;
//            }
//
//            if (hasHeader(xhr, /HX-Redirect:/i)) {
//                location.href = xhr.getResponseHeader("HX-Redirect");
//                return;
//            }
//
//            if (hasHeader(xhr,/HX-Refresh:/i)) {
//                if ("true" === xhr.getResponseHeader("HX-Refresh")) {
//                    location.reload();
//                    return;
//                }
//            }
//
//            if (hasHeader(xhr,/HX-Retarget:/i)) {
//                responseInfo.target = getDocument().querySelector(xhr.getResponseHeader("HX-Retarget"));
//            }
//
//            var historyUpdate = determineHistoryUpdates(elt, responseInfo);
//
//            // by default htmx only swaps on 200 return codes and does not swap
//            // on 204 'No Content'
//            // this can be ovverriden by responding to the htmx:beforeSwap event and
//            // overriding the detail.shouldSwap property
//            var shouldSwap = xhr.status >= 200 && xhr.status < 400 && xhr.status !== 204;
//            var serverResponse = xhr.response;
//            var isError = xhr.status >= 400;
//            var beforeSwapDetails = mergeObjects({shouldSwap: shouldSwap, serverResponse:serverResponse, isError:isError}, responseInfo);
//            if (!triggerEvent(target, 'htmx:beforeSwap', beforeSwapDetails)) return;
//
//            target = beforeSwapDetails.target; // allow re-targeting
//            serverResponse = beforeSwapDetails.serverResponse; // allow updating content
//            isError = beforeSwapDetails.isError; // allow updating error
//
//            responseInfo.target = target; // Make updated target available to response events
//            responseInfo.failed = isError; // Make failed property available to response events
//            responseInfo.successful = !isError; // Make successful property available to response events
//
//            if (beforeSwapDetails.shouldSwap) {
//                if (xhr.status === 286) {
//                    cancelPolling(elt);
//                }
//
//                withExtensions(elt, function (extension) {
//                    serverResponse = extension.transformResponse(serverResponse, xhr, elt);
//                });
//
//                // Save current page if there will be a history update
//                if (historyUpdate.type) {
//                    saveCurrentPageToHistory();
//                }
//
//                var swapOverride = etc.swapOverride;
//                if (hasHeader(xhr,/HX-Reswap:/i)) {
//                    swapOverride = xhr.getResponseHeader("HX-Reswap");
//                }
//                var swapSpec = getSwapSpecification(elt, swapOverride);
//
//                target.classList.add(htmx.config.swappingClass);
//
//                // optional transition API promise callbacks
//                var settleResolve = null;
//                var settleReject = null;
//
//                var doSwap = function () {
//                    try {
//                        var activeElt = document.activeElement;
//                        var selectionInfo = {};
//                        try {
//                            selectionInfo = {
//                                elt: activeElt,
//                                // @ts-ignore
//                                start: activeElt ? activeElt.selectionStart : null,
//                                // @ts-ignore
//                                end: activeElt ? activeElt.selectionEnd : null
//                            };
//                        } catch (e) {
//                            // safari issue - see https://github.com/microsoft/playwright/issues/5894
//                        }
//
//                        var settleInfo = makeSettleInfo(target);
//                        selectAndSwap(swapSpec.swapStyle, target, elt, serverResponse, settleInfo);
//
//                        if (selectionInfo.elt &&
//                            !bodyContains(selectionInfo.elt) &&
//                            selectionInfo.elt.id) {
//                            var newActiveElt = document.getElementById(selectionInfo.elt.id);
//                            var focusOptions = { preventScroll: swapSpec.focusScroll !== undefined ? !swapSpec.focusScroll : !htmx.config.defaultFocusScroll };
//                            if (newActiveElt) {
//                                // @ts-ignore
//                                if (selectionInfo.start && newActiveElt.setSelectionRange) {
//                                    // @ts-ignore
//                                    try {
//                                        newActiveElt.setSelectionRange(selectionInfo.start, selectionInfo.end);
//                                    } catch (e) {
//                                        // the setSelectionRange method is present on fields that don't support it, so just let this fail
//                                    }
//                                }
//                                newActiveElt.focus(focusOptions);
//                            }
//                        }
//
//                        target.classList.remove(htmx.config.swappingClass);
//                        forEach(settleInfo.elts, function (elt) {
//                            if (elt.classList) {
//                                elt.classList.add(htmx.config.settlingClass);
//                            }
//                            triggerEvent(elt, 'htmx:afterSwap', responseInfo);
//                        });
//
//                        if (hasHeader(xhr, /HX-Trigger-After-Swap:/i)) {
//                            var finalElt = elt;
//                            if (!bodyContains(elt)) {
//                                finalElt = getDocument().body;
//                            }
//                            handleTrigger(xhr, "HX-Trigger-After-Swap", finalElt);
//                        }
//
//                        var doSettle = function () {
//                            forEach(settleInfo.tasks, function (task) {
//                                task.call();
//                            });
//                            forEach(settleInfo.elts, function (elt) {
//                                if (elt.classList) {
//                                    elt.classList.remove(htmx.config.settlingClass);
//                                }
//                                triggerEvent(elt, 'htmx:afterSettle', responseInfo);
//                            });
//
//                            // if we need to save history, do so
//                            if (historyUpdate.type) {
//                                if (historyUpdate.type === "push") {
//                                    pushUrlIntoHistory(historyUpdate.path);
//                                    triggerEvent(getDocument().body, 'htmx:pushedIntoHistory', {path: historyUpdate.path});
//                                } else {
//                                    replaceUrlInHistory(historyUpdate.path);
//                                    triggerEvent(getDocument().body, 'htmx:replacedInHistory', {path: historyUpdate.path});
//                                }
//                            }
//                            if (responseInfo.pathInfo.anchor) {
//                                var anchorTarget = find("#" + responseInfo.pathInfo.anchor);
//                                if(anchorTarget) {
//                                    anchorTarget.scrollIntoView({block:'start', behavior: "auto"});
//                                }
//                            }
//
//                            if(settleInfo.title) {
//                                var titleElt = find("title");
//                                if(titleElt) {
//                                    titleElt.innerHTML = settleInfo.title;
//                                } else {
//                                    window.document.title = settleInfo.title;
//                                }
//                            }
//
//                            updateScrollState(settleInfo.elts, swapSpec);
//
//                            if (hasHeader(xhr, /HX-Trigger-After-Settle:/i)) {
//                                var finalElt = elt;
//                                if (!bodyContains(elt)) {
//                                    finalElt = getDocument().body;
//                                }
//                                handleTrigger(xhr, "HX-Trigger-After-Settle", finalElt);
//                            }
//                            maybeCall(settleResolve);
//                        }
//
//                        if (swapSpec.settleDelay > 0) {
//                            setTimeout(doSettle, swapSpec.settleDelay)
//                        } else {
//                            doSettle();
//                        }
//                    } catch (e) {
//                        triggerErrorEvent(elt, 'htmx:swapError', responseInfo);
//                        maybeCall(settleReject);
//                        throw e;
//                    }
//                };
//
//                var shouldTransition = htmx.config.globalViewTransitions
//                if(swapSpec.hasOwnProperty('transition')){
//                    shouldTransition = swapSpec.transition;
//                }
//
//                if(shouldTransition &&
//                    triggerEvent(elt, 'htmx:beforeTransition', responseInfo) &&
//                    typeof Promise !== "undefined" && document.startViewTransition){
//                    var settlePromise = new Promise(function (_resolve, _reject) {
//                        settleResolve = _resolve;
//                        settleReject = _reject;
//                    });
//                    // wrap the original doSwap() in a call to startViewTransition()
//                    var innerDoSwap = doSwap;
//                    doSwap = function() {
//                        document.startViewTransition(function () {
//                            innerDoSwap();
//                            return settlePromise;
//                        });
//                    }
//                }
//
//
//                if (swapSpec.swapDelay > 0) {
//                    setTimeout(doSwap, swapSpec.swapDelay)
//                } else {
//                    doSwap();
//                }
//            }
//            if (isError) {
//                triggerErrorEvent(elt, 'htmx:responseError', mergeObjects({error: "Response Status Error Code " + xhr.status + " from " + responseInfo.pathInfo.requestPath}, responseInfo));
//            }
//        }
//
//        //====================================================================
//        // Extensions API
//        //====================================================================
//
//        /** @type {Object<string, import("./htmx").HtmxExtension>} */
//        var extensions = {};
//
//        /**
//         * extensionBase defines the default functions for all extensions.
//         * @returns {import("./htmx").HtmxExtension}
//         */
//        function extensionBase() {
//            return {
//                init: function(api) {return null;},
//                onEvent : function(name, evt) {return true;},
//                transformResponse : function(text, xhr, elt) {return text;},
//                isInlineSwap : function(swapStyle) {return false;},
//                handleSwap : function(swapStyle, target, fragment, settleInfo) {return false;},
//                encodeParameters : function(xhr, parameters, elt) {return null;}
//            }
//        }
//
//        /**
//         * defineExtension initializes the extension and adds it to the htmx registry
//         *
//         * @param {string} name
//         * @param {import("./htmx").HtmxExtension} extension
//         */
//        function defineExtension(name, extension) {
//            if(extension.init) {
//                extension.init(internalAPI)
//            }
//            extensions[name] = mergeObjects(extensionBase(), extension);
//        }
//
//        /**
//         * removeExtension removes an extension from the htmx registry
//         *
//         * @param {string} name
//         */
//        function removeExtension(name) {
//            delete extensions[name];
//        }
//
//        /**
//         * getExtensions searches up the DOM tree to return all extensions that can be applied to a given element
//         *
//         * @param {HTMLElement} elt
//         * @param {import("./htmx").HtmxExtension[]=} extensionsToReturn
//         * @param {import("./htmx").HtmxExtension[]=} extensionsToIgnore
//         */
//         function getExtensions(elt, extensionsToReturn, extensionsToIgnore) {
//
//            if (elt == undefined) {
//                return extensionsToReturn;
//            }
//            if (extensionsToReturn == undefined) {
//                extensionsToReturn = [];
//            }
//            if (extensionsToIgnore == undefined) {
//                extensionsToIgnore = [];
//            }
//            var extensionsForElement = getAttributeValue(elt, "hx-ext");
//            if (extensionsForElement) {
//                forEach(extensionsForElement.split(","), function(extensionName){
//                    extensionName = extensionName.replace(/ /g, '');
//                    if (extensionName.slice(0, 7) == "ignore:") {
//                        extensionsToIgnore.push(extensionName.slice(7));
//                        return;
//                    }
//                    if (extensionsToIgnore.indexOf(extensionName) < 0) {
//                        var extension = extensions[extensionName];
//                        if (extension && extensionsToReturn.indexOf(extension) < 0) {
//                            extensionsToReturn.push(extension);
//                        }
//                    }
//                });
//            }
//            return getExtensions(parentElt(elt), extensionsToReturn, extensionsToIgnore);
//        }
//
//        //====================================================================
//        // Initialization
//        //====================================================================
//
//        function ready(fn) {
//            if (getDocument().readyState !== 'loading') {
//                fn();
//            } else {
//                getDocument().addEventListener('DOMContentLoaded', fn);
//            }
//        }
//
//        function insertIndicatorStyles() {
//            if (htmx.config.includeIndicatorStyles !== false) {
//                getDocument().head.insertAdjacentHTML("beforeend",
//                    "<style>\
//                      ." + htmx.config.indicatorClass + "{opacity:0;transition: opacity 200ms ease-in;}\
//                      ." + htmx.config.requestClass + " ." + htmx.config.indicatorClass + "{opacity:1}\
//                      ." + htmx.config.requestClass + "." + htmx.config.indicatorClass + "{opacity:1}\
//                    </style>");
//            }
//        }
//
//        function getMetaConfig() {
//            var element = getDocument().querySelector('meta[name="htmx-config"]');
//            if (element) {
//                // @ts-ignore
//                return parseJSON(element.content);
//            } else {
//                return null;
//            }
//        }
//
//        function mergeMetaConfig() {
//            var metaConfig = getMetaConfig();
//            if (metaConfig) {
//                htmx.config = mergeObjects(htmx.config , metaConfig)
//            }
//        }
//
//        // initialize the document
//        ready(function () {
//            mergeMetaConfig();
//            insertIndicatorStyles();
//            var body = getDocument().body;
//            processNode(body);
//            var restoredElts = getDocument().querySelectorAll(
//                "[hx-trigger='restored'],[data-hx-trigger='restored']"
//            );
//            body.addEventListener("htmx:abort", function (evt) {
//                var target = evt.target;
//                var internalData = getInternalData(target);
//                if (internalData && internalData.xhr) {
//                    internalData.xhr.abort();
//                }
//            });
//            var originalPopstate = window.onpopstate;
//            window.onpopstate = function (event) {
//                if (event.state && event.state.htmx) {
//                    restoreHistory();
//                    forEach(restoredElts, function(elt){
//                        triggerEvent(elt, 'htmx:restored', {
//                            'document': getDocument(),
//                            'triggerEvent': triggerEvent
//                        });
//                    });
//                } else {
//                    if (originalPopstate) {
//                        originalPopstate(event);
//                    }
//                }
//            };
//            setTimeout(function () {
//                triggerEvent(body, 'htmx:load', {}); // give ready handlers a chance to load up before firing this event
//                body = null; // kill reference for gc
//            }, 0);
//        })
//
//        return htmx;
//    }
//)()
//}));