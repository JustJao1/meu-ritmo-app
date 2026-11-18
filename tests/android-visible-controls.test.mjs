import {test} from 'node:test';
import assert from 'node:assert/strict';
import {readFileSync} from 'node:fs';

const base=new URL('../android/app/src/main/',import.meta.url);
const source=readFileSync(new URL('java/br/com/joaovictor/meuritmolite/OnlineActivity.java',base),'utf8');
const theme=readFileSync(new URL('res/values/styles.xml',base),'utf8');

test('native controls use compact, accessible app-styled actions',()=>{
 assert.ok(theme.includes('Theme.Material.Light.NoActionBar'));
 assert.ok(source.includes('Button alarmButton=new Button(this)'));
 assert.ok(source.includes('alarmButton.setText("Alarmes do aparelho")'));
 assert.ok(source.includes('alarmButton.setOnClickListener(v->startActivity(new Intent(this,MainActivity.class)))'));
 assert.ok(source.includes('refreshButton.setOnClickListener(v->web.reload())'));
 assert.ok(source.includes('root.addView(controls,new LinearLayout.LayoutParams(-1,-2))'));
 assert.ok(source.includes('alarmButton.setBackgroundResource(R.drawable.mr_control_secondary)'));
 assert.ok(source.includes('refreshButton.setBackgroundResource(R.drawable.mr_control_primary)'));
 assert.ok(source.includes('R.drawable.mr_alarm'));
 assert.ok(source.includes('R.drawable.mr_refresh'));
 assert.ok(source.includes('screenWidthDp>=600'));
 assert.ok(source.includes('WindowInsets.Type.systemBars()'));
 assert.ok(source.includes('setStateListAnimator(null)'));
});
