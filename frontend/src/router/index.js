import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Dashboard from '../views/Dashboard.vue'
import SupplyDemand from '../views/SupplyDemand.vue'
import PreSale from '../views/PreSale.vue'
import Article from '../views/Article.vue'
import Community from '../views/Community.vue'
import AiConsult from '../views/AiConsult.vue'
import Admin from '../views/Admin.vue'
import Profile from '../views/Profile.vue'
import Stats from '../views/Stats.vue'
import Trace from '../views/Trace.vue'
import Price from '../views/Price.vue'
import Message from '../views/Message.vue'
import Order from '../views/Order.vue'
import Favorite from '../views/Favorite.vue'
import Subscription from '../views/Subscription.vue'
import Search from '../views/Search.vue'
import Cert from '../views/Cert.vue'
import Point from '../views/Point.vue'
import Recommend from '../views/Recommend.vue'

const routes = [
  { path: '/', component: Dashboard },
  { path: '/login', component: Login },
  { path: '/supply', component: SupplyDemand },
  { path: '/presale', component: PreSale },
  { path: '/article', component: Article },
  { path: '/community', component: Community },
  { path: '/ai', component: AiConsult },
  { path: '/admin', component: Admin },
  { path: '/mine', component: Profile },
  { path: '/stats', component: Stats },
  { path: '/trace', component: Trace },
  { path: '/price', component: Price },
  { path: '/message', component: Message },
  { path: '/order', component: Order },
  { path: '/favorite', component: Favorite },
  { path: '/subscribe', component: Subscription },
  { path: '/search', component: Search },
  { path: '/cert', component: Cert },
  { path: '/point', component: Point },
  { path: '/recommend', component: Recommend }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) next('/login')
  else next()
})

export default router
